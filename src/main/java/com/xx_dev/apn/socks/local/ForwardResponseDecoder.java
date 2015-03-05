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

import com.xx_dev.apn.socks.common.ForwardResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.handler.codec.socks.SocksProtocolVersion;
import io.netty.handler.codec.socks.SocksResponse;
import io.netty.util.CharsetUtil;

import java.util.List;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.local.ForwardResponseDecoder 2015-03-05 19:15 (xmx) Exp $
 */
public class ForwardResponseDecoder extends ReplayingDecoder<ForwardResponseDecoder.STATE> {
    private static final String name = "FORWARD_RESPONSE_DECODER";

    /**
     * @deprecated Will be removed at the next minor version bump.
     */
    @Deprecated
    public static String getName() {
        return name;
    }

    private int streamId;
    private SocksCmdStatus cmdStatus;


    public ForwardResponseDecoder() {
        super(STATE.READ_STREAM_ID);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        switch (state()) {
        case READ_STREAM_ID: {
            streamId = byteBuf.readShort();
            checkpoint(STATE.READ_CMD_STATUS);
        }
        case READ_CMD_STATUS: {
            cmdStatus = SocksCmdStatus.valueOf(byteBuf.readByte());
        }
        }
        ctx.pipeline().remove(this);
        out.add(new ForwardResponse(streamId, cmdStatus));
    }

    enum STATE {
        READ_STREAM_ID,
        READ_CMD_STATUS
    }
}
