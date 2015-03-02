/*
 * Copyright (c) 2014 The APN-PROXY Project
 *
 * The APN-PROXY Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.xx_dev.apn.socks.local;

import com.xx_dev.apn.socks.common.FrameDecoder;
import com.xx_dev.apn.socks.common.FrameEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponseDecoder;
import io.netty.handler.codec.socks.SocksInitResponseDecoder;
import io.netty.handler.codec.socks.SocksMessageEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.Promise;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.local.ForwardClientInitializer 2015-03-02 20:05 (xmx) Exp $
 */
public class ForwardClientInitializer extends ChannelInitializer<SocketChannel> {

    private final Promise<Channel> promise;
    private final SocksCmdRequest socksCmdRequest;

    public ForwardClientInitializer(Promise<Channel> promise, SocksCmdRequest socksCmdRequest) {
        this.promise = promise;
        this.socksCmdRequest = socksCmdRequest;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();

        p.addLast(new FrameDecoder());
        p.addLast(new FrameEncoder());

        p.addLast(new SocksCmdResponseDecoder());
        p.addLast(new SocksMessageEncoder());
        p.addLast(new ForwardClientHandler(promise, socksCmdRequest));
    }

}
