package com.alibaba.dubbo.remoting.transport.netty4.http2;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.exchange.Request;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2ConnectionHandler;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2LocalFlowController;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.util.AsciiString;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http2.Http2CodecUtil.getEmbeddedHttp2Exception;

/**
 * @author yiji
 */
public abstract class AbstractHttp2CodecHandler extends Http2ConnectionHandler{

    public static final AsciiString STATUS_OK = AsciiString.of("200");
    public static final AsciiString HTTP_METHOD = AsciiString.of("POST");
    public static final AsciiString HTTP_GET_METHOD = AsciiString.of("GET");
    public static final AsciiString HTTPS = AsciiString.of("https");
    public static final AsciiString HTTP = AsciiString.of("http");
    public static final AsciiString CONTENT_TYPE_HEADER = AsciiString.of("content-type");
    public static final AsciiString TE_HEADER = AsciiString.of("te");
    public static final AsciiString TE_TRAILERS = AsciiString.of("trailers");
    public static final AsciiString USER_AGENT = AsciiString.of("user-agent");
    static final Object NOOP_MESSAGE = new Object();
    private static final long BDP_MEASUREMENT_PING = 1234;
    private final FlowControlPinger flowControlPing = new FlowControlPinger();
    protected URL url;
    private int initialConnectionWindow;
    private ChannelHandlerContext ctx;
    private boolean autoTuneFlowControlOn = false;

    public AbstractHttp2CodecHandler(URL url,
                                     Http2ConnectionDecoder decoder,
                                     Http2ConnectionEncoder encoder,
                                     Http2Settings initialSettings) {
        super(decoder, encoder, initialSettings);
        this.url = url;
        this.initialConnectionWindow = initialSettings.initialWindowSize() == null ? -1 :
                initialSettings.initialWindowSize();
    }

    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        super.handlerAdded(ctx);
        sendInitialConnectionWindow();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        sendInitialConnectionWindow();
    }

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Http2Exception embedded = getEmbeddedHttp2Exception(cause);
        if (embedded == null) {
            // There was no embedded Http2Exception, assume it's a connection error. Subclasses are
            // responsible for storing the appropriate status and shutting down the connection.
            onError(ctx, false, cause);
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    /**
     * Sends initial connection window to the remote endpoint if necessary.
     */
    private void sendInitialConnectionWindow() throws Http2Exception {
        if (ctx.channel().isActive() && initialConnectionWindow > 0) {
            Http2Stream connectionStream = connection().connectionStream();
            int currentSize = connection().local().flowController().windowSize(connectionStream);
            int delta = initialConnectionWindow - currentSize;
            decoder().flowController().incrementWindowSize(connectionStream, delta);
            initialConnectionWindow = -1;
            ctx.flush();
        }
    }

    protected Http2Stream requireHttp2Stream(int streamId) {
        Http2Stream stream = connection().stream(streamId);
        if (stream == null) {
            // This should never happen.
            throw new AssertionError("Stream does not exist: " + streamId);
        }
        return stream;
    }

    protected Http2Headers prepareHeaders(Request request, Payload payload) {
        Http2Headers headers = new DefaultHttp2Headers();
        if (!request.isEvent()) {
            headers.path(
                    "/" + payload.header(Constants.HTTP2_SERVICE_KEY) +
                            "/" + payload.header(Constants.HTTP2_SERVICE_METHOD_KEY)
            ).scheme(payload.header(Constants.HTTP2_SCHEME_KEY))
                    .authority(Constants.HTTP2_AUTHORITY_KEY)
                    .method(Constants.POST_KEY)
                    .add(Constants.HTTP2_USER_AGENT_KEY, "");

            if (payload.attachments() != null) {
                for (Map.Entry<String, String> attachment : payload.attachments().entrySet()) {
                    headers.add(attachment.getKey(), attachment.getValue());
                }
            }

        }
        return headers;
    }

    protected FlowControlPinger flowControlPing() {
        return flowControlPing;
    }

    protected final ChannelHandlerContext ctx() {
        return ctx;
    }

    public void setAutoTuneFlowControl(boolean isOn) {
        this.autoTuneFlowControlOn = isOn;
    }

    protected boolean isDubboContentType(String contentType) {

        if (contentType == null) {
            return false;
        }

        contentType = contentType.toLowerCase();
        if (!contentType.startsWith(Constants.HTTP2_DUBBO_CONTENT_TYPE)) {
            return false;
        }

        if (contentType.length() == Constants.HTTP2_DUBBO_CONTENT_TYPE.length()) {
            return true;
        }

        char nextChar = contentType.charAt(Constants.HTTP2_DUBBO_CONTENT_TYPE.length());
        return nextChar == '+' || nextChar == ';';
    }

    public void handleProtocolNegotiationCompleted(Object event) {

    }

    public final class FlowControlPinger {

        private static final int MAX_WINDOW_SIZE = 8 * 1024 * 1024;
        private int pingCount;
        private int pingReturn;
        private boolean pinging;
        private int dataSizeSincePing;
        private float lastBandwidth; // bytes per second
        private long lastPingTime;

        public long payload() {
            return BDP_MEASUREMENT_PING;
        }

        public int maxWindow() {
            return MAX_WINDOW_SIZE;
        }

        public void onDataRead(int dataLength, int paddingLength) {
            if (!autoTuneFlowControlOn) {
                return;
            }
            if (!isPinging()) {
                setPinging(true);
                sendPing(ctx());
            }
            incrementDataSincePing(dataLength + paddingLength);
        }

        public void updateWindow() throws Http2Exception {
            if (!autoTuneFlowControlOn) {
                return;
            }
            pingReturn++;
            long elapsedTime = (System.nanoTime() - lastPingTime);
            if (elapsedTime == 0) {
                elapsedTime = 1;
            }
            long bandwidth = (getDataSincePing() * TimeUnit.SECONDS.toNanos(1)) / elapsedTime;
            Http2LocalFlowController fc = decoder().flowController();
            // Calculate new window size by doubling the observed BDP, but cap at max window
            int targetWindow = Math.min(getDataSincePing() * 2, MAX_WINDOW_SIZE);
            setPinging(false);
            int currentWindow = fc.initialWindowSize(connection().connectionStream());
            if (targetWindow > currentWindow && bandwidth > lastBandwidth) {
                lastBandwidth = bandwidth;
                int increase = targetWindow - currentWindow;
                fc.incrementWindowSize(connection().connectionStream(), increase);
                fc.initialWindowSize(targetWindow);
                Http2Settings settings = new Http2Settings();
                settings.initialWindowSize(targetWindow);
                frameWriter().writeSettings(ctx(), settings, ctx().newPromise());
            }

        }

        private boolean isPinging() {
            return pinging;
        }

        private void setPinging(boolean pingOut) {
            pinging = pingOut;
        }

        private void sendPing(ChannelHandlerContext ctx) {
            setDataSizeSincePing(0);
            lastPingTime = System.nanoTime();
            encoder().writePing(ctx, false, BDP_MEASUREMENT_PING, ctx.newPromise());
            pingCount++;
        }

        private void incrementDataSincePing(int increase) {
            int currentSize = getDataSincePing();
            setDataSizeSincePing(currentSize + increase);
        }

        int getPingCount() {
            return pingCount;
        }

        int getPingReturn() {
            return pingReturn;
        }

        int getDataSincePing() {
            return dataSizeSincePing;
        }

        void setDataSizeSincePing(int dataSize) {
            dataSizeSincePing = dataSize;
        }
    }
}
