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

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdResponseDecoder;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.handler.codec.socks.SocksCmdType;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Promise;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.local.ForwardClientHandler 2015-03-02 19:47 (xmx) Exp $
 */
public class ForwardClientHandler extends SimpleChannelInboundHandler<SocksCmdResponse> {
    private final Promise<Channel> promise;
    private final SocksCmdRequest socksCmdRequest;

    public ForwardClientHandler(Promise<Channel> promise, SocksCmdRequest socksCmdRequest) {
        this.promise = promise;
        this.socksCmdRequest = socksCmdRequest;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(socksCmdRequest);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksCmdResponse socksCmdResponse) throws Exception {
            if (socksCmdResponse.cmdStatus() == SocksCmdStatus.SUCCESS) {
                ctx.pipeline().remove(this);
                promise.setSuccess(ctx.channel());
            } else {
                ctx.close();
            }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        promise.setFailure(throwable);
    }
}
