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

package com.xx_dev.apn.socks.test;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksAuthScheme;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdResponseDecoder;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.handler.codec.socks.SocksCmdType;
import io.netty.handler.codec.socks.SocksInitRequest;
import io.netty.handler.codec.socks.SocksResponse;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.test.SocksClientHandler 2015-02-28 15:41 (xmx) Exp $
 */
public class SocksClientHandler extends SimpleChannelInboundHandler<SocksResponse> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.writeAndFlush(new SocksInitRequest(new ArrayList<SocksAuthScheme>(0)));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksResponse socksResponse) throws Exception {
        switch (socksResponse.responseType()) {
        case INIT: {
            ctx.pipeline().addAfter("log", "cmdResponseDecoder", new SocksCmdResponseDecoder());
            ctx.write(new SocksCmdRequest(SocksCmdType.CONNECT, SocksAddressType.DOMAIN, "www.baidu.com", 80));
            break;
        }
        case AUTH:
            ctx.pipeline().addAfter("log", "cmdResponseDecoder", new SocksCmdResponseDecoder());
            ctx.write(new SocksCmdRequest(SocksCmdType.CONNECT, SocksAddressType.DOMAIN, "www.baidu.com", 80));
            break;
        case CMD:
            SocksCmdResponse res = (SocksCmdResponse) socksResponse;
            if (res.cmdStatus() == SocksCmdStatus.SUCCESS) {
                ctx.pipeline().addLast(new SocksClientConnectHandler());
                ctx.pipeline().remove(this);
                //ctx.fireChannelRead(socksResponse);

                String s = "GET / HTTP/1.1\r\nHOST: www.baidu.com\r\n\r\n";

                ctx.writeAndFlush(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));
            } else {
                ctx.close();
            }
            break;
        case UNKNOWN:
            ctx.close();
            break;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        ctx.flush();
    }
}
