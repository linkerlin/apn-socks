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

package com.xx_dev.apn.socks.remote;

import com.xx_dev.apn.socks.common.DirectClientHandler;
import com.xx_dev.apn.socks.common.ForwardMsg;
import com.xx_dev.apn.socks.common.ForwardRelayMsg;
import com.xx_dev.apn.socks.common.ForwardRequest;
import com.xx_dev.apn.socks.common.ForwardResponse;
import com.xx_dev.apn.socks.common.ForwardRelayHandler;
import com.xx_dev.apn.socks.util.SocksServerUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ApnSocksRemoteServerConnectHandler extends SimpleChannelInboundHandler<ForwardMsg> {
    private static final Logger logger = Logger.getLogger(ApnSocksRemoteServerConnectHandler.class);


    private Map<Integer, Channel> map = new ConcurrentHashMap<Integer, Channel>();

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final ForwardMsg forwardMsg) throws Exception {
        if (forwardMsg.type() == 1) {
            directConnect(ctx, (ForwardRequest)forwardMsg);
        } else if (forwardMsg.type() == 0) {
            relay(ctx, (ForwardRelayMsg)forwardMsg);
        }
    }

    private void relay(ChannelHandlerContext ctx, ForwardRelayMsg forwardMsg) {
        logger.warn("relay: " + forwardMsg.streamId());
        map.get(forwardMsg.streamId()).writeAndFlush(forwardMsg);
    }

    private void directConnect(final ChannelHandlerContext ctx, final ForwardRequest request) throws Exception {
        Promise<Channel> promise = ctx.executor().newPromise();
        promise.addListener(
                new GenericFutureListener<Future<Channel>>() {
                    @Override
                    public void operationComplete(final Future<Channel> future) throws Exception {
                        final Channel outboundChannel = future.getNow();
                        if (future.isSuccess()) {
                            map.put(request.streamId(), outboundChannel);
                            ctx.channel()
                               .writeAndFlush(new ForwardResponse(request.streamId(), SocksCmdStatus.SUCCESS))
                               .addListener(new ChannelFutureListener() {
                                   @Override
                                   public void operationComplete(ChannelFuture channelFuture) {
                                       outboundChannel.pipeline().addLast(new ForwardRelayHandler(request.streamId(), ctx.channel()));
                                   }
                               });
                        } else {
                            ctx.channel()
                               .writeAndFlush(new ForwardResponse(request.streamId(), SocksCmdStatus.FAILURE));
                            SocksServerUtils.closeOnFlush(ctx.channel());
                        }
                    }
                });

        final Channel inboundChannel = ctx.channel();
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
         .channel(NioSocketChannel.class)
         .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
         .option(ChannelOption.SO_KEEPALIVE, true)
         .handler(new DirectClientHandler(promise));

        b.connect(request.host(), request.port()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    // Connection established use handler provided results
                } else {
                    // Close the connection if the connection attempt has failed.
                    ctx.channel().writeAndFlush(
                            new ForwardResponse(request.streamId(), SocksCmdStatus.FAILURE));
                    SocksServerUtils.closeOnFlush(ctx.channel());
                }
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SocksServerUtils.closeOnFlush(ctx.channel());
    }
}
