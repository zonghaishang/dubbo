package com.alibaba.dubbo.remoting.transport.netty4.http2;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientUpgradeHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandshakeCompletionEvent;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.AsciiString;

import javax.net.ssl.SSLException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.CertificateException;

/**
 * @author yiji
 */
public abstract class AbstractHttp2Initializer<C extends Channel> extends ChannelInitializer<C> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHttp2Initializer.class);
    protected SslContext sslCtx;
    protected int maxHttpContentLength;
    protected URL url;

    public AbstractHttp2Initializer(URL url) throws SSLException, CertificateException {

        this.url = url;
        this.maxHttpContentLength = url.getParameter(Constants.MAX_HTTP_CONTENT_BYTES_KEY, Constants.MAX_HTTP_CONTENT_BYTES);

        if (url.getParameter(Constants.SSL_ENABLE_KEY, false)) {
            String certificate = url.getParameter(Constants.SSL_CERTIFICATE_KEY);
            String privateKey = url.getParameter(Constants.SSL_PRIVATE_KEY);
            SslContextBuilder sslContextBuilder = getSSLContextBuilder(url, certificate, privateKey);
            this.sslCtx = sslContextBuilder.sslProvider(OpenSsl.isAlpnSupported() ? SslProvider.OPENSSL : SslProvider.JDK)
                    /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification. */
                    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    .applicationProtocolConfig(new ApplicationProtocolConfig(
                            ApplicationProtocolConfig.Protocol.ALPN,
                            ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                            ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                            ApplicationProtocolNames.HTTP_2,
                            ApplicationProtocolNames.HTTP_1_1)).build();
        }
    }

    protected SslContextBuilder getSSLContextBuilder(URL url, String certificate, String privateKey) throws CertificateException {
        SslContextBuilder sslContextBuilder;
        if (detectSSL(certificate, privateKey)) {
            sslContextBuilder = SslContextBuilder.forServer(new File(certificate), new File(privateKey), url.getParameter(Constants.SSL_PRIVATE_KEY_PASSWORD));
        } else {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            sslContextBuilder = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey());
            logger.warn("Not found certificate" + StringUtils.nullToEmpty(certificate)
                    + " or private key file " + StringUtils.nullToEmpty(privateKey) + ", "
                    + "will be created an temporary self-signed certificate for testing purposes, "
                    + "It is not recommended to use in production environment.");
        }
        return sslContextBuilder;
    }

    @Override
    protected void initChannel(C channel) throws Exception {
        if (sslCtx != null) {
            configureSsl(channel);
        } else {
            configureClearText(channel);
        }
        channel.pipeline().addLast(new Http2NegotiatorHandler());
    }

    protected void negotiateComplete(C channel) {

    }

    // ssl configured ?
    protected boolean detectSSL(String certificate, String privateKey) {
        return StringUtils.isNotEmpty(certificate) && StringUtils.isNotEmpty(privateKey)
                && Files.exists(Paths.get(certificate)) && Files.exists(Paths.get(privateKey));
    }

    protected abstract void configureSsl(C channel);

    protected abstract void configureClearText(C channel);

    /**
     * Class that logs any User Events triggered on this channel.
     */
    private class Http2NegotiatorHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            logger.info("Debug user event: " + evt);
            if (evt == HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_SUCCESSFUL) {
                ctx.pipeline().remove(Http2NegotiatorHandler.this);
                negotiateComplete((C) ctx.channel());
            } else if (evt == SslHandshakeCompletionEvent.SUCCESS) {
                ctx.pipeline().remove(Http2NegotiatorHandler.this);
                negotiateComplete((C) ctx.channel());
            } else if (evt instanceof HttpServerUpgradeHandler.UpgradeEvent) {
                // handle plain text upgrade
                HttpServerUpgradeHandler.UpgradeEvent upgradeEvent = (HttpServerUpgradeHandler.UpgradeEvent) evt;
                if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, upgradeEvent.protocol())) {
                    ctx.pipeline().remove(Http2NegotiatorHandler.this);
                    negotiateComplete((C) ctx.channel());
                }
            } else if (evt == HttpClientUpgradeHandler.UpgradeEvent.UPGRADE_REJECTED) {
                logger.error("HTTP/2 upgrade rejected , evt: " + evt);
                ctx.close();
            }
            super.userEventTriggered(ctx, evt);
        }
    }
}
