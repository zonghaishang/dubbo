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
import io.netty.channel.Channel;
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
import io.netty.handler.codec.http2.StreamBufferingEncoder;
import io.netty.handler.codec.http2.WeightedFairQueueByteDistributor;
import io.netty.handler.logging.LogLevel;
import io.netty.util.AsciiString;

import java.net.InetSocketAddress;
import java.util.Map;

import static io.netty.handler.codec.http2.DefaultHttp2LocalFlowController.DEFAULT_WINDOW_UPDATE_RATIO;
import static io.netty.util.CharsetUtil.UTF_8;

public class NettyHttp2ClientHandler extends AbstractHttp2CodecHandler {

    private final Logger logger = LoggerFactory.getLogger(NettyHttp2ClientHandler.class);

    Http2Connection.PropertyKey streamKey;

    private NettyHttp2ClientHandler(URL url,
                                    Http2ConnectionDecoder decoder,
                                    StreamBufferingEncoder encoder,
                                    Http2Settings settings) {
        super(url, decoder, encoder, settings);

        Http2Connection connection = encoder.connection();
        connection.addListener(new Http2ConnectionAdapter() {
            @Override
            public void onGoAwayReceived(int lastStreamId, long errorCode, ByteBuf debugData) {
                byte[] debugDataBytes = ByteBufUtil.getBytes(debugData);
                goingAway(lastStreamId, errorCode, debugData);
                if (errorCode == Http2Error.ENHANCE_YOUR_CALM.code()) {
                    String data = new String(debugDataBytes, UTF_8);
                    logger.warn("Received GOAWAY with ENHANCE_YOUR_CALM. Debug data: " + data);
                }
            }

            @Override
            public void onStreamActive(Http2Stream stream) {
                if (connection().numActiveStreams() != 1) {
                    return;
                }
            }

            @Override
            public void onStreamClosed(Http2Stream stream) {
                if (connection().numActiveStreams() != 0) {
                    return;
                }
            }
        });

        this.streamKey = encoder.connection().newKey();
        this.decoder().frameListener(new NettyHttp2ClientHandler.FrameListener());
    }

    public static NettyHttp2ClientHandler newHandler(URL url) {
        Http2Settings initialSettings = Http2Settings.defaultSettings();
        initialSettings.pushEnabled(false);
        Http2HeadersDecoder headersDecoder = new DefaultHttp2HeadersDecoder(true, initialSettings.maxHeaderListSize());
        Http2FrameReader frameReader = new DefaultHttp2FrameReader(headersDecoder);
        Http2FrameWriter frameWriter = new DefaultHttp2FrameWriter();
        Http2Connection connection = new DefaultHttp2Connection(false);
        WeightedFairQueueByteDistributor distributor = new WeightedFairQueueByteDistributor(connection);
        distributor.allocationQuantum(16 * 1024);
        DefaultHttp2RemoteFlowController controller =
                new DefaultHttp2RemoteFlowController(connection, distributor);
        connection.remote().flowController(controller);
        return newHandler(url, connection, frameReader, frameWriter, initialSettings);
    }

    public static NettyHttp2ClientHandler newHandler(URL url, final Http2Connection connection,
                                                     Http2FrameReader frameReader,
                                                     Http2FrameWriter frameWriter,
                                                     Http2Settings settings) {

        Http2FrameLogger frameLogger = new Http2FrameLogger(LogLevel.DEBUG, NettyHttp2ClientHandler.class);
        frameReader = new Http2InboundFrameLogger(frameReader, frameLogger);
        frameWriter = new Http2OutboundFrameLogger(frameWriter, frameLogger);

        StreamBufferingEncoder encoder = new StreamBufferingEncoder(new DefaultHttp2ConnectionEncoder(connection, frameWriter));

        // Create the local flow controller configured to auto-refill the connection window.
        connection.local().flowController(
                new DefaultHttp2LocalFlowController(connection, DEFAULT_WINDOW_UPDATE_RATIO, true));

        Http2ConnectionDecoder decoder = new DefaultHttp2ConnectionDecoder(connection, encoder, frameReader);

        return new NettyHttp2ClientHandler(url, decoder, encoder, settings);
    }

    private void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, boolean endStream) throws Http2Exception {
        try {

            Http2Stream http2Stream = requireHttp2Stream(streamId);
            Payload payload = http2Stream.getProperty(streamKey);

            if (payload == null) {
                // new rpc request from remote, maybe heartbeat
                logger.warn("client side received remote headers from stream: " + streamId);
                return;
            }

            Channel channel = ctx.channel();

//            Integer httpStatus = headers.getInt(Constants.HTTP2_STATUS_KEY);
//            if (httpStatus == null) {
//                throw new RemotingException((InetSocketAddress) channel.localAddress(),
//                        (InetSocketAddress) channel.remoteAddress(), "Missing HTTP status code");
//            }
//
//            if (httpStatus >= 100 && httpStatus < 200) {
//                // Ignore the header. See RFC 7540 §8.1
//                return;
//            }

            CharSequence contentType = headers.get(CONTENT_TYPE_HEADER);
            if (contentType == null) {
                throw new RemotingException((InetSocketAddress) channel.localAddress(),
                        (InetSocketAddress) channel.remoteAddress(), "Content-Type is missing from the request");
            }

            payload.streamId(streamId)
                    .http2Headers(headers)
                    .endOfStream(endStream);
        } catch (Exception e) {
            logger.warn("Unexpected onHeaderRead.", e);
            throw Http2Exception.streamError(
                    streamId, Http2Error.INTERNAL_ERROR, e, StringUtils.nullToEmpty(e.getMessage()));
        }
    }

    private void onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream)
            throws Http2Exception {
        Channel channel = ctx.channel();
        try {
            flowControlPing().onDataRead(data.readableBytes(), padding);
            Payload payload = clientStream(requireHttp2Stream(streamId));
            if (payload == null) {
                // new rpc request from remote, maybe heartbeat
                logger.warn("client side received remote data from stream: " + streamId);
                return;
            }

            payload.data(data.retain()).endOfStream(endOfStream);

            if (payload.http2Headers() == null) {
                // received before header ??
                throw new RemotingException((InetSocketAddress) ctx.channel().localAddress(),
                        (InetSocketAddress) ctx.channel().remoteAddress(), "Headers not received before payload.");
            }

            channel.attr(Payload.KEY).set(payload);
            ctx.fireChannelRead(payload.data());
        } catch (Throwable e) {
            logger.warn("Exception in onDataRead()", e);
            throw Http2Exception.streamError(
                    streamId, Http2Error.INTERNAL_ERROR, e, StringUtils.nullToEmpty(e.getMessage()));
        } finally {
            if (channel != null) {
                channel.attr(Payload.KEY).set(null);
            }
        }
    }

    /**
     * Handler for a GOAWAY being received. Fails any streams created after the last known http2Stream.
     */
    private void goingAway(int lastStreamId, long errorCode, ByteBuf debugData) {
        final int lastKnownStream = connection().local().lastStreamKnownByPeer();
        try {
            connection().forEachActiveStream(new Http2StreamVisitor() {
                @Override
                public boolean visit(Http2Stream stream) throws Http2Exception {
                    if (stream.id() > lastKnownStream) {
                        Payload clientStream = clientStream(stream);
                        if (clientStream != null) {
                            stream.removeProperty(streamKey);
                        }
                        stream.close();
                    }
                    return true;
                }
            });
        } catch (Http2Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        Payload payload;
        Channel channel = ctx.channel();
        if ((payload = channel.attr(Payload.KEY).get()) == null) {
            promise.setFailure(new RemotingException((InetSocketAddress) channel.localAddress(),
                    (InetSocketAddress) channel.remoteAddress(), "Ignore to write because of not found payload from key 'http2.payload'"));
            return;
        }

        Object message = payload.message();
        ChannelBuffer buffer = payload.encodedBuffer();

        if (message instanceof Request) {
            sendRpcRequest(ctx, (Request) message, payload, promise);
        } else if (message instanceof Response) {
            sendRpcResponse(ctx, (Response) message, payload, promise);
        } else if (message == NOOP_MESSAGE) {
            ctx.writeAndFlush(Unpooled.EMPTY_BUFFER, promise);
        }
    }

    private void sendRpcRequest(ChannelHandlerContext ctx, Request request, final Payload payload, final ChannelPromise promise) throws Exception {

        // Get the http2Stream ID for the new http2Stream.
        final int streamId;
        try {
            streamId = incrementAndGetNextStreamId();
            payload.streamId(streamId).endOfStream(!request.isTwoWay());

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
    }

    private void sendRpcResponse(ChannelHandlerContext ctx, Response response, final Payload payload, final ChannelPromise promise) throws Exception {

        // Get the http2Stream ID for the new http2Stream.
        final int streamId = payload.streamId();

        try {
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
            byte[] sendBytes = new byte[payload.encodedBuffer().readableBytes()];
            payload.encodedBuffer().readBytes(sendBytes);

            encoder().writeData(ctx, streamId, Unpooled.wrappedBuffer(sendBytes), 0, payload.endOfStream(), promise);
        } catch (Exception e) {
            promise.setFailure(e);
        }
    }

    protected Http2Headers prepareHeaders(Request request, Payload payload) {
        Http2Headers headers = new DefaultHttp2Headers();

        String contentType = payload.header(Constants.HTTP2_CONTENT_TYPE_KEY, true);
        headers.add(Constants.HTTP2_CONTENT_TYPE_KEY, contentType == null ? Constants.HTTP2_DUBBO_CONTENT_TYPE : contentType);

        if (!request.isEvent()) {
            headers.path(
                    "/" + payload.header(Constants.HTTP2_SERVICE_KEY, true) +
                            "/" + payload.header(Constants.HTTP2_SERVICE_METHOD_KEY, true)
            ).scheme(payload.header(Constants.HTTP2_SCHEME_KEY, true))
                    .authority(payload.header(Constants.HTTP2_AUTHORITY_KEY, true))
                    .method(Constants.POST_KEY);

            // .add(payload.header(Constants.HTTP2_USER_AGENT_KEY, true));

            if (payload.attachments() != null) {
                for (Map.Entry<String, String> attachment : payload.attachments().entrySet()) {
                    headers.add(attachment.getKey(), attachment.getValue());
                }
            }
        }
        return headers;
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

    private void onRstStreamRead(int streamId, long errorCode) throws Http2Exception {
        Payload payload = clientStream(connection().stream(streamId));
        if (payload != null) {

        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        try {
            logger.warn("Network closed for unknown reason");
            // Report status to the application layer for any open streams
            connection().forEachActiveStream(new Http2StreamVisitor() {
                @Override
                public boolean visit(Http2Stream stream) throws Http2Exception {
                    Payload clientStream = clientStream(stream);
                    if (clientStream != null) {
                        stream.removeProperty(streamKey);
                    }
                    return true;
                }
            });
        } finally {
            // Close any open streams
            super.channelInactive(ctx);
        }
    }

    private Payload clientStream(Http2Stream stream) {
        return stream == null ? null : (Payload) stream.getProperty(streamKey);
    }

    private void respondWithHttpError(
            ChannelHandlerContext ctx, int streamId, int code, Payload.Status.Code statusCode, String msg) {

        Http2Headers headers = new DefaultHttp2Headers(true, 2)
                .status("" + code)
                .set(CONTENT_TYPE_HEADER, "text/plain; encoding=utf-8");

        headers.add(new AsciiString("status".getBytes(), false), new AsciiString(statusCode.name().getBytes(), false));
        headers.add(new AsciiString("message".getBytes(), false), new AsciiString(msg.getBytes(), false));

        encoder().writeHeaders(ctx, streamId, headers, 0, false, ctx.newPromise());
        ByteBuf msgBuf = ByteBufUtil.writeUtf8(ctx.alloc(), msg);
        encoder().writeData(ctx, streamId, msgBuf, 0, true, ctx.newPromise());
    }

    private class FrameListener extends Http2FrameAdapter {

        @Override
        public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
            NettyHttp2ClientHandler.this.onDataRead(ctx, streamId, data, padding, endOfStream);
            return data.readableBytes() + padding;
        }

        @Override
        public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                                  short weight, boolean exclusive, int padding, boolean endStream) throws Http2Exception {
            NettyHttp2ClientHandler.this.onHeadersRead(ctx, streamId, headers, endStream);
        }

        @Override
        public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode)
                throws Http2Exception {
            NettyHttp2ClientHandler.this.onRstStreamRead(streamId, errorCode);
        }
    }
}