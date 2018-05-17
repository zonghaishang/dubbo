package com.alibaba.dubbo.remoting.transport.netty4.http2;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.Http2ClientUpgradeCodec;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * @author yiji
 */
public class NettyHttp2ClientInitalizer extends AbstractHttp2Initializer<SocketChannel> {

    // private static final Http2FrameLogger logger = new Http2FrameLogger(INFO, NettyHttp2ClientInitalizer.class);

    private static final Logger logger = LoggerFactory.getLogger(NettyHttp2ClientInitalizer.class);

    //    private Http2SettingsHandler settingsHandler;
    private NettyHttp2ClientHandler connectionHandler;

    public NettyHttp2ClientInitalizer(URL url) throws SSLException, CertificateException {
        super(url);
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        this.connectionHandler = NettyHttp2ClientHandler.newHandler(url);
        super.initChannel(channel);
    }


    /**
     * Configure the pipeline for TLS NPN negotiation to HTTP/2.
     */
    @Override
    protected void configureSsl(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(sslCtx.newHandler(ch.alloc()));
        // We must wait for the handshake to finish and the protocol to be negotiated before configuring
        // the HTTP/2 components of the pipeline.
        pipeline.addLast(new ApplicationProtocolNegotiationHandler("") {
            @Override
            protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                    ChannelPipeline p = ctx.pipeline();
                    // p.addLast(connectionHandler);
                    configureEndOfPipeline(p);
                    return;
                }
                ctx.close();
                throw new IllegalStateException("unknown protocol: " + protocol);
            }
        });
    }

    /**
     * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.
     */
    @Override
    protected void configureClearText(SocketChannel ch) {
        HttpClientCodec httpClientCodec = new HttpClientCodec();
        Http2ClientUpgradeCodec upgradeCodec = new Http2ClientUpgradeCodec(connectionHandler);
        HttpClientUpgradeHandler upgrader = new HttpClientUpgradeHandler(httpClientCodec, upgradeCodec, maxHttpContentLength);

        ch.pipeline().addLast(httpClientCodec, upgrader, new UpgradeRequestHandler());
    }

    protected SslContextBuilder getSSLContextBuilder(URL url, String certificate, String privateKey) throws CertificateException {
        return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE);
    }

    protected void configureEndOfPipeline(ChannelPipeline pipeline) {
        pipeline.addLast(connectionHandler);
    }

    private final class UpgradeRequestHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            DefaultFullHttpRequest upgradeRequest =
                    new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");
            ctx.writeAndFlush(upgradeRequest);
            ctx.pipeline().remove(this);
            ctx.fireChannelActive();
        }
    }
}
