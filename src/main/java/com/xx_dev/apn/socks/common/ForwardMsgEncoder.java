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

package com.xx_dev.apn.socks.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.socks.SocksMessage;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.common.ForwardMsgEncoder 2015-03-05 19:12 (xmx) Exp $
 */
@Sharable
public class ForwardMsgEncoder extends MessageToByteEncoder<ForwardMsg> {
    private static final String name = "FORWARD_MESSAGE_ENCODER";

    /**
     * @deprecated Will be removed at the next minor version bump.
     */
    @Deprecated
    public static String getName() {
        return name;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void encode(ChannelHandlerContext ctx, ForwardMsg msg, ByteBuf out) throws Exception {
        msg.encodeAsByteBuf(out);
    }
}
