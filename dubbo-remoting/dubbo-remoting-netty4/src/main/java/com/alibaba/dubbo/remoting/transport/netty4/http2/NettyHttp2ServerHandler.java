package com.alibaba.dubbo.remoting.transport.netty4.http2;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DefaultHttp2ConnectionDecoder;
import io.netty.handler.codec.http2.DefaultHttp2ConnectionEncoder;
import io.netty.handler.codec.http2.DefaultHttp2FrameReader;
import io.netty.handler.codec.http2.DefaultHttp2FrameWriter;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersDecoder;
import io.netty.handler.codec.http2.DefaultHttp2LocalFlowController;
import io.netty.handler.codec.http2.DefaultHttp2RemoteFlowController;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionAdapter;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameAdapter;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2FrameReader;
import io.netty.handler.codec.http2.Http2FrameWriter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersDecoder;
import io.netty.handler.codec.http2.Http2InboundFrameLogger;
import io.netty.handler.codec.http2.Http2OutboundFrameLogger;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.handler.codec.http2.Http2StreamVisitor;
import io.netty.handler.codec.http2.WeightedFairQueueByteDistributor;
import io.netty.handler.logging.LogLevel;

import java.net.InetSocketAddress;

import static io.netty.handler.codec.http2.DefaultHttp2LocalFlowController.DEFAULT_WINDOW_UPDATE_RATIO;


public class NettyHttp2ServerHandler extends AbstractHttp2CodecHandler {

    private final Logger logger = LoggerFactory.getLogger(NettyHttp2ServerHandler.class);

    Http2Connection.PropertyKey streamKey;

    private NettyHttp2ServerHandler(URL url,
                                    final Http2Connection connection,
                                    Http2ConnectionDecoder decoder,
                                    Http2ConnectionEncoder encoder,
                                    Http2Settings initialSettings) {
        super(url, decoder, encoder, initialSettings);

        connection.addListener(new Http2ConnectionAdapter() {
            @Override
            public void onStreamActive(Http2Stream stream) {
                if (connection.numActiveStreams() == 1) {

                }
            }

            @Override
            public void onStreamClosed(Http2Stream stream) {
                if (connection.numActiveStreams() == 0) {

                }
            }
        });

        this.streamKey = encoder.connection().newKey();
        this.decoder().frameListener(new NettyHttp2ServerHandler.FrameListener());
    }

    public static NettyHttp2ServerHandler newHandler(URL url) {
        Http2FrameLogger frameLogger = new Http2FrameLogger(LogLevel.DEBUG, NettyHttp2ServerHandler.class);
        Http2Settings initialSettings = Http2Settings.defaultSettings();
        Http2HeadersDecoder headersDecoder = new DefaultHttp2HeadersDecoder(true, initialSettings.maxHeaderListSize());
        Http2FrameReader frameReader = new Http2InboundFrameLogger(new DefaultHttp2FrameReader(headersDecoder), frameLogger);
        Http2FrameWriter frameWriter = new Http2OutboundFrameLogger(new DefaultHttp2FrameWriter(), frameLogger);
        return newHandler(url, frameReader, frameWriter);
    }

    public static NettyHttp2ServerHandler newHandler(URL url, Http2FrameReader frameReader, Http2FrameWriter frameWriter) {
        final Http2Connection connection = new DefaultHttp2Connection(true);
        WeightedFairQueueByteDistributor dist = new WeightedFairQueueByteDistributor(connection);
        dist.allocationQuantum(16 * 1024); // Make benchmarks fast again.
        DefaultHttp2RemoteFlowController controller =
                new DefaultHttp2RemoteFlowController(connection, dist);
        connection.remote().flowController(controller);

        // Create the local flow controller configured to auto-refill the connection window.
        connection.local().flowController(
                new DefaultHttp2LocalFlowController(connection, DEFAULT_WINDOW_UPDATE_RATIO, true));

        Http2ConnectionEncoder encoder = new DefaultHttp2ConnectionEncoder(connection, frameWriter);
        Http2ConnectionDecoder decoder = new DefaultHttp2ConnectionDecoder(connection, encoder,
                frameReader);

        Http2Settings settings = new Http2Settings();
//        settings.initialWindowSize(flowControlWindow);
//        settings.maxConcurrentStreams(maxStreams);
//        settings.maxHeaderListSize(maxHeaderListSize);
        return new NettyHttp2ServerHandler(url, connection, decoder, encoder, settings);
    }

    private void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, boolean endStream) throws Http2Exception {
        try {
            CharSequence path = headers.path();
            if (path == null) {
                respondWithHttpError(ctx, streamId, 404, Payload.Status.Code.UNIMPLEMENTED,
                        "Request path is missing, expect patten '/service/method'");
                return;
            }

            if (path.charAt(0) != '/') {
                respondWithHttpError(ctx, streamId, 404, Payload.Status.Code.UNIMPLEMENTED,
                        String.format("Expected path to start with /: %s", path));
                return;
            }

            // Verify that the Content-Type is correct in the request.
            CharSequence contentType = headers.get(CONTENT_TYPE_HEADER);
            if (contentType == null) {
                respondWithHttpError(
                        ctx, streamId, 415, Payload.Status.Code.INTERNAL, "Content-Type is missing from the request");
                return;
            }

            String contentTypeString = contentType.toString();
            if (!isDubboContentType(contentTypeString)) {
                respondWithHttpError(ctx, streamId, 415, Payload.Status.Code.INTERNAL,
                        String.format("Content-Type '%s' is not supported", contentTypeString));
                return;
            }

            if (!Constants.POST_KEY.equals(headers.method().toString())) {
                respondWithHttpError(ctx, streamId, 405, Payload.Status.Code.INTERNAL,
                        String.format("Method '%s' is not supported", headers.method()));
                return;
            }

            Http2Stream http2Stream = requireHttp2Stream(streamId);
            Payload payload = new Payload(http2Stream, headers);
            payload.streamId(streamId)
                    .endOfStream(endStream);
            http2Stream.setProperty(streamKey, payload);
            ctx.channel().attr(Payload.KEY).set(payload);
        } catch (Exception e) {
            logger.warn("Unexpected onHeaderRead.", e);
            throw Http2Exception.streamError(
                    streamId, Http2Error.INTERNAL_ERROR, e, StringUtils.nullToEmpty(e.getMessage()));
        }
    }

    private void onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream)
            throws Http2Exception {
        try {
            flowControlPing().onDataRead(data.readableBytes(), padding);
            Payload payload = serverStream(requireHttp2Stream(streamId));

            if (payload == null || payload.streamId() != streamId) {
                // never happen ？
                logger.error((payload == null ? "Payload is not found" : ("Payload stream id is " + payload.streamId() + ", expected " + streamId)));
                return;
            }

            payload.data(data.retain())
                    .endOfStream(endOfStream);
            ctx.fireChannelRead(payload.data());
        } catch (Throwable e) {
            logger.warn("Exception in onDataRead()", e);
            throw Http2Exception.streamError(
                    streamId, Http2Error.INTERNAL_ERROR, e, StringUtils.nullToEmpty(e.getMessage()));
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        Payload payload = null;
        if ((payload = ctx.channel().attr(Payload.KEY).get()) == null) {
            promise.setFailure(new RemotingException(
                    (InetSocketAddress) ctx.channel().localAddress(),
                    (InetSocketAddress) ctx.channel().remoteAddress(), " not found payload from key 'http2.payload'"));
            return;
        }

        Object message = payload.message();
        ChannelBuffer buffer = payload.encodedBuffer();

        if (message instanceof Response) {
            sendRpcResponse(ctx, (Response) message, payload, promise);
        } else if (message instanceof Request) {
            // may be event ?
            sendRpcRequest(ctx, (Request) message, payload, promise);
        }
    }

    private void sendRpcRequest(ChannelHandlerContext ctx, Request request, final Payload payload, final ChannelPromise promise) throws Exception {

        // Get the http2Stream ID for the new http2Stream.
        final int streamId;
        try {
            streamId = incrementAndGetNextStreamId();
        } catch (RemotingException e) {
            promise.setFailure(e);
            // Initiate a graceful shutdown if we haven't already.
            if (!connection().goAwaySent()) {
                logger.warn("Stream IDs have been exhausted for this connection. "
                        + "Initiating graceful shutdown of the connection.");
                close(ctx, promise);
            }
            return;
        }

        payload.streamId(streamId);

        Http2Headers headers = prepareHeaders(request, payload);

        payload.http2Headers(headers);
        ChannelPromise callbackPromise = ctx().newPromise();
        encoder().writeHeaders(ctx(), streamId, headers, 0, !request.isTwoWay(), callbackPromise)
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            // The http2Stream will be null in case a http2Stream buffered in the encoder was canceled via RST_STREAM.
                            Http2Stream http2Stream = connection().stream(streamId);
                            if (http2Stream != null) {
                                http2Stream.setProperty(streamKey, payload);
                                // Attach the client http2Stream to the HTTP/2 http2Stream object as user data.
                                payload.http2Stream(http2Stream);
                            }
                        } else {
                            promise.setFailure(future.cause());
                        }
                    }
                });


        // dubbo channel buffer is not easy to use when in netty.
        // dubbo default is not use direct buffer.
        byte[] sendBytes = new byte[payload.encodedBuffer().readableBytes()];
        payload.encodedBuffer().readBytes(sendBytes);

        encoder().writeData(ctx, streamId, Unpooled.wrappedBuffer(sendBytes), 0, !request.isTwoWay(), promise);
    }

    private void sendRpcResponse(ChannelHandlerContext ctx, Response response, final Payload payload, final ChannelPromise promise) throws Exception {

        // Get the http2Stream ID for the new http2Stream.
        final int streamId = payload.streamId();
        Http2Stream stream = connection().stream(streamId);

        if (stream == null) {
            resetStream(ctx, streamId, Http2Error.CANCEL.code(), promise);
            return;
        }

        if (payload.endOfStream()) {
            stream.removeProperty(streamKey);
        }

        Http2Headers headers = payload.http2Headers();
        encoder().writeHeaders(ctx, streamId, headers, 0, payload.endOfStream(), ctx.voidPromise());

        // dubbo channel buffer is not easy to use when in netty.
        // dubbo default is not use direct buffer.
        byte[] sendBytes = new byte[payload.encodedBuffer().readableBytes()];
        payload.encodedBuffer().readBytes(sendBytes);

        encoder().writeData(ctx, streamId, Unpooled.wrappedBuffer(sendBytes), 0, true, promise);
    }

    private void onRstStreamRead(int streamId, long errorCode) throws Http2Exception {
        Payload payload = serverStream(connection().stream(streamId));
        if (payload != null) {

        }
    }

    @Override
    protected void onStreamError(ChannelHandlerContext ctx, boolean outbound, Throwable cause,
                                 Http2Exception.StreamException http2Ex) {
        logger.warn("Stream Error", cause);
        Http2Stream stream = connection().stream(Http2Exception.streamId(http2Ex));
        Payload payload = serverStream(stream);
        if (payload != null) {
            stream.removeProperty(streamKey);
        }
        // Delegate to the base class to send a RST_STREAM.
        super.onStreamError(ctx, outbound, cause, http2Ex);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            logger.warn("connection terminated for unknown reason. ");
            // Any streams that are still active must be closed
            connection().forEachActiveStream(new Http2StreamVisitor() {
                @Override
                public boolean visit(Http2Stream stream) throws Http2Exception {
                    Payload serverStream = serverStream(stream);
                    if (serverStream != null) {
                        stream.removeProperty(streamKey);
                    }
                    return true;
                }
            });
        } finally {
            super.channelInactive(ctx);
        }
    }

    private int incrementAndGetNextStreamId() throws RemotingException {
        int nextStreamId = connection().local().incrementAndGetNextStreamId();
        if (nextStreamId < 0) {
            logger.error("Stream IDs have been exhausted for this connection. "
                    + "Initiating graceful shutdown of the connection.");
            throw new RemotingException(null, "Stream IDs have been exhausted");
        }
        return nextStreamId;
    }

    private Payload serverStream(Http2Stream stream) {
        return stream == null ? null : (Payload) stream.getProperty(streamKey);
    }

    private void respondWithHttpError(
            ChannelHandlerContext ctx, int streamId, int code, Payload.Status.Code statusCode, String message) {

        Http2Headers headers = new DefaultHttp2Headers(true, 2)
                .status("" + code)
                .set(CONTENT_TYPE_HEADER, "text/plain; encoding=utf-8");

        headers.add("status", statusCode.name());
        headers.add("message", message);

        encoder().writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise());
        ByteBuf msgBuf = ByteBufUtil.writeUtf8(ctx.alloc(), message);
        encoder().writeData(ctx, streamId, msgBuf, 0, true, ctx.newPromise());
    }

    private class FrameListener extends Http2FrameAdapter {

        @Override
        public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
            NettyHttp2ServerHandler.this.onDataRead(ctx, streamId, data, padding, endOfStream);
            return data.readableBytes() + padding;
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                                  short weight, boolean exclusive, int padding, boolean endStream) throws Http2Exception {
            NettyHttp2ServerHandler.this.onHeadersRead(ctx, streamId, headers, endStream);
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream) throws Http2Exception {
            NettyHttp2ServerHandler.this.onHeadersRead(ctx, streamId, headers, endStream);
        }

        @Override
        public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode)
                throws Http2Exception {
            NettyHttp2ServerHandler.this.onRstStreamRead(streamId, errorCode);
        }
    }
}