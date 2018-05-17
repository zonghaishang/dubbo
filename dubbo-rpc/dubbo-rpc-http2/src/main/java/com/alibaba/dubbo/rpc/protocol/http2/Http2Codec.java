/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.protocol.http2;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.transport.netty4.NettyChannel;
import com.alibaba.dubbo.remoting.transport.netty4.http2.Payload;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboCodec;

import io.netty.handler.codec.http2.Http2Headers;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dubbo codec.
 */
public class Http2Codec extends DubboCodec implements Codec2 {

    public static final String NAME = "http2";
    public static final String DUBBO_VERSION = Version.getVersion(Http2Codec.class, Version.getVersion());
    public static final byte RESPONSE_WITH_EXCEPTION = 0;
    public static final byte RESPONSE_VALUE = 1;
    public static final byte RESPONSE_NULL_VALUE = 2;
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private static final Logger logger = LoggerFactory.getLogger(Http2Codec.class);

    // request/response id -> streamId
    private ConcurrentHashMap<Long, Integer> streamIdMap = new ConcurrentHashMap<Long, Integer>();

    @Override
    public void encode(Channel channel, ChannelBuffer buffer, Object message) throws IOException {
        if(channel instanceof NettyChannel){
            NettyChannel nettyChannel = (NettyChannel)channel;
            Payload payload = nettyChannel.getNettyAttribute(Payload.KEY);
            if(payload == null){
                nettyChannel.setNettyAttribute(Payload.KEY, payload = new Payload(message, buffer));
            }else {
                if(message instanceof Response){
                    int streamId = streamIdMap.get(((Response) message).getId());
                    payload.streamId(streamId).message(message).encodedBuffer(buffer);
                }
            }
        }
        super.encode(channel, buffer, message);
    }

    @Override
    protected void encodeRequestData(Channel channel, ObjectOutput out, Object data) throws IOException {
        super.encodeRequestData(channel, out, data);

        RpcInvocation invoker = (RpcInvocation) data;
        URL url = invoker.getInvoker().getUrl();
        String serviceName = invoker.getAttachment(Constants.PATH_KEY);
        String methodName = invoker.getMethodName();

        if(channel instanceof NettyChannel){
            NettyChannel nettyChannel = (NettyChannel)channel;

            // prepare header for http2
            Payload payload = nettyChannel.getNettyAttribute(Payload.KEY);
            if(payload == null) {
                // throw new RemoteException() ?
                logger.error("Not found stream payload for key 'http2.payload'");
                return;
            }
            payload.addAttachment(Constants.HTTP2_SERVICE_KEY, serviceName)
                    .addAttachment(Constants.HTTP2_SERVICE_METHOD_KEY, methodName)
                    .addAttachment(Constants.HTTP2_SCHEME_KEY, url.getParameter(Constants.SSL_ENABLE_KEY, false) ? "https" : "http")
                    .addAttachment(Constants.HTTP2_AUTHORITY_KEY, url.getAddress())
                    .addAttachment(Constants.HTTP2_CONTENT_TYPE_KEY, Constants.HTTP2_DUBBO_CONTENT_TYPE);
        }
    }

    // Will be invoked when decode body
    @Override
    protected void attachInvocation(Channel channel, Invocation invocation, long id) {
        if(channel instanceof NettyChannel){
            NettyChannel nettyChannel = (NettyChannel)channel;
            Payload payload = nettyChannel.getNettyAttribute(Payload.KEY);

            if (payload != null && payload.http2Headers() != null && invocation instanceof RpcInvocation) {
                Http2Headers headers = payload.http2Headers();
                streamIdMap.put(id, payload.streamId());
                // inject any headers
                System.out.println(headers);
            }
        }
    }
}
