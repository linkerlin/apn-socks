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

import com.xx_dev.apn.socks.common.ForwardMsg;
import com.xx_dev.apn.socks.common.ForwardRelayMsg;
import com.xx_dev.apn.socks.common.ForwardRequest;
import com.xx_dev.apn.socks.common.ForwardResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Promise;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.local.ForwardClientHandler 2015-03-02 19:47 (xmx) Exp $
 */
public class ForwardClientHandler extends SimpleChannelInboundHandler<ForwardMsg> {

    public ForwardClientHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ForwardMsg forwardMsg) throws Exception {
        if (forwardMsg.type() == 0 ) {
            ForwardClientManager.ins().relay(forwardMsg.streamId(), ((ForwardRelayMsg)forwardMsg).relayMsgByteBuf());
        } else if (forwardMsg.type() == 2 && ((ForwardResponse)forwardMsg).cmdStatus() == SocksCmdStatus.SUCCESS) {
            ForwardClientManager.ins().responseForwardConnectSuccess(forwardMsg.streamId(), ctx.channel());
        } else if (forwardMsg.type() == 3) {
            ForwardClientManager.ins().closeInboundChannel(forwardMsg.streamId());
        } else {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
        //promise.setFailure(throwable);
    }
}
