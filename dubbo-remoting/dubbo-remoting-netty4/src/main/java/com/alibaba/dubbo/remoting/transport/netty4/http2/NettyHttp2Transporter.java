package com.alibaba.dubbo.remoting.transport.netty4.http2;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.Transporter;

/**
 * @author yiji
 */
public class NettyHttp2Transporter implements Transporter {

    public static final String NAME = "http2";

    @Override
    public Server bind(URL url, ChannelHandler listener) throws RemotingException {
        return new NettyHttp2Server(url, listener);
    }

    @Override
    public Client connect(URL url, ChannelHandler listener) throws RemotingException {
        return new NettyHttp2Client(url, listener);
    }


}
