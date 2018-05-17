package com.alibaba.dubbo.remoting.transport.netty4.http2;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.netty4.NettyClient;
import com.alibaba.dubbo.remoting.transport.netty4.NettyClientHandler;
import com.alibaba.dubbo.remoting.transport.netty4.NettyCodecAdapter;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author yiji
 */
public class NettyHttp2Client extends NettyClient {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttp2Client.class);
    NettyHttp2ClientInitalizer http2ClientInitalizer;

    public NettyHttp2Client(URL url, ChannelHandler handler) throws RemotingException {
        super(url, handler);
    }

    @Override
    protected void doOpen() throws Throwable {
        final NettyClientHandler nettyClientHandler = new NettyClientHandler(getUrl(), this);
        bootstrap = new Bootstrap();
        bootstrap.group(nioEventLoopGroup)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeout())
                .channel(NioSocketChannel.class);

        if (getTimeout() < 3000) {
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
        } else {
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, getConnectTimeout());
        }

        bootstrap.handler(new NettyHttp2ClientInitalizer(getUrl()) {
            @Override
            protected void negotiateComplete(SocketChannel ch) {
                NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec(), getUrl(), NettyHttp2Client.this);
                channel.pipeline()
                        .addLast("decoder", adapter.getDecoder())
                        .addLast("encoder", adapter.getEncoder())
                        .addLast("handler", nettyClientHandler);
            }
        });
    }
}
