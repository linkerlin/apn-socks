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

import com.xx_dev.apn.socks.common.ForwardFinMsg;
import com.xx_dev.apn.socks.common.ForwardRelayHandler;
import com.xx_dev.apn.socks.common.ForwardRequest;
import com.xx_dev.apn.socks.util.LoggerUtil;
import com.xx_dev.apn.socks.util.SocksServerUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.local.ForwardClientManager 2015-03-06 20:33 (xmx) Exp $
 */
public class ForwardClientManager {
    private static final Logger logger = Logger.getLogger(ForwardClientManager.class);

    private static final ForwardClientManager ins = new ForwardClientManager();

    private Channel forwardChanne;
    private AtomicInteger streamId = new AtomicInteger(1);
    private Map<Integer, ChannelHandlerContext> map = new ConcurrentHashMap<Integer, ChannelHandlerContext>();

    private ForwardClientManager() {

    }

    public static ForwardClientManager ins() {
        return ins;
    }

    public void forwardRequest(final ChannelHandlerContext inboundCtx, final SocksCmdRequest request) {
        final int currentStreamId = streamId.getAndAdd(1);
        if (forwardChanne == null || !forwardChanne.isActive()) {
            Bootstrap b = new Bootstrap();
            b.group(inboundCtx.channel().eventLoop())
             .channel(NioSocketChannel.class)
             .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
             .option(ChannelOption.SO_KEEPALIVE, true)
             .handler(new ForwardClientInitializer());

            b.connect("apnsocks.test.server", 8889).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        map.put(currentStreamId, inboundCtx);


                        // Connection established use handler provided results
                        forwardChanne = future.channel();

                        forwardChanne.writeAndFlush(
                                new ForwardRequest(currentStreamId, request.addressType(), request.host(),
                                                   request.port()));
                    } else {
                        // Close the connection if the connection attempt has failed.
                        inboundCtx.channel().writeAndFlush(
                                new SocksCmdResponse(SocksCmdStatus.FAILURE, request.addressType()));
                        SocksServerUtils.closeOnFlush(inboundCtx.channel());
                    }
                }
            });
        } else {
            map.put(currentStreamId, inboundCtx);
            forwardChanne.writeAndFlush(
                    new ForwardRequest(currentStreamId, request.addressType(), request.host(), request.port()));
        }
    }

    public void responseForwardConnectSuccess(final int streamId, final Channel outboundChannel) {
        LoggerUtil.info(logger, "FCS: " + streamId);
        map.get(streamId).channel()
           .writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, SocksAddressType.IPv4))
           .addListener(new ChannelFutureListener() {
               @Override
               public void operationComplete(ChannelFuture channelFuture) {
                   if (channelFuture.isSuccess()) {
                       try {
                           map.get(streamId).pipeline().remove(ApnSocksLocalServerConnectHandler.NAME);
                       } catch (NoSuchElementException e) {
                           LoggerUtil.error(logger,
                                            e.getMessage() + ", " + streamId + ", " + map.get(streamId).isRemoved() +
                                            ", " + map.get(streamId).channel().isActive());
                       }

                       map.get(streamId).pipeline().addLast(new ForwardRelayHandler(streamId, outboundChannel));
                   } else {
                       outboundChannel.writeAndFlush(new ForwardFinMsg(streamId));
                   }
               }
           });

    }

    public void responseForwardConnectFail(final int streamId, final Channel outboundChannel) {
        LoggerUtil.info(logger, "FCF: " + streamId);
        map.get(streamId).channel()
           .writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, SocksAddressType.IPv4));
        SocksServerUtils.closeOnFlush(map.get(streamId).channel());
    }

    public void relay(final int streamId, final ByteBuf byteBuf) {
        ByteBuf _byteBuf = Unpooled.copiedBuffer(byteBuf);
        ChannelHandlerContext ctx = map.get(streamId);
        ctx.channel().writeAndFlush(_byteBuf);
        byteBuf.release();
    }

    public void closeInboundChannel(int streamId) {
        SocksServerUtils.closeOnFlush(map.get(streamId).channel());
        map.remove(streamId);
    }
}
