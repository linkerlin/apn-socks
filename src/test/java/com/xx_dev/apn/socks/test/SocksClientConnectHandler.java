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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.test.SocksClientConnectHandler 2015-02-28 16:38 (xmx) Exp $
 */
public class SocksClientConnectHandler extends SimpleChannelInboundHandler<SocksCmdResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksCmdResponse msg) throws Exception {

    }
}
