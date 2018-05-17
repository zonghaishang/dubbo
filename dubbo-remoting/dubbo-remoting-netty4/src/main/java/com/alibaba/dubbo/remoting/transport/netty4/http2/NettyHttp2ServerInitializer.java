package com.alibaba.dubbo.remoting.transport.netty4.http2;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.util.AsciiString;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;

/**
 * @author yiji
 */
public class NettyHttp2ServerInitializer extends AbstractHttp2Initializer<NioSocketChannel> {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttp2ServerInitializer.class);
    final HttpServerUpgradeHandler.UpgradeCodecFactory upgradeCodecFactory = new HttpServerUpgradeHandler.UpgradeCodecFactory() {
        @Override
        public HttpServerUpgradeHandler.UpgradeCodec newUpgradeCodec(CharSequence protocol) {
            if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
                return new Http2ServerUpgradeCodec(NettyHttp2ServerHandler.newHandler(url));
            } else {
                return null;
            }
        }
    };

    public NettyHttp2ServerInitializer(URL url) throws SSLException, CertificateException {
        super(url);
    }

    /**
     * Configure the pipeline for TLS NPN negotiation to HTTP/2.
     */
    @Override
    protected void configureSsl(NioSocketChannel ch) {
        ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()), NettyHttp2ServerHandler.newHandler(url));
    }

    /**
     * Configure the pipeline for a cleartext upgrade from HTTP to HTTP/2.0
     */
    @Override
    protected void configureClearText(NioSocketChannel channel) {
        final ChannelPipeline p = channel.pipeline();
        final HttpServerCodec httpServerCodec = new HttpServerCodec();
        final HttpServerUpgradeHandler upgradeHandler = new HttpServerUpgradeHandler(httpServerCodec, upgradeCodecFactory);
        final CleartextHttp2ServerUpgradeHandler cleartextHttp2ServerUpgradeHandler =
                new CleartextHttp2ServerUpgradeHandler(httpServerCodec, upgradeHandler,
                        NettyHttp2ServerHandler.newHandler(url));

        channel.pipeline().addLast(cleartextHttp2ServerUpgradeHandler);
    }
}
