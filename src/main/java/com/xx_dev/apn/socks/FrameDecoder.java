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

package com.xx_dev.apn.socks;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.FrameDecoder 2015-02-28 18:25 (xmx) Exp $
 */
public class FrameDecoder extends ReplayingDecoder<FrameDecoder.STATE> {

    enum STATE {
        READ_MAGIC_NUMBER,
        READ_LENGTH,
        READ_CONTENT
    }

    private int length;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (this.state()) {
        case READ_MAGIC_NUMBER: {
            int magicNumber = in.readInt();
            if (magicNumber != 0x34ed2b11) {
                throw new Exception("Wrong magic number!");
            }
            this.checkpoint(STATE.READ_LENGTH);
        }
        case READ_LENGTH: {
            length = in.readInt();
            if (length > 1024*512 + 1000) {
                ctx.close();
            }
            this.checkpoint(STATE.READ_CONTENT);
        }
        case READ_CONTENT: {
            byte[] data = new byte[length];
            in.readBytes(data, 0, length);
            byte[] raw = data;
            ByteBuf outBuf = ctx.alloc().buffer();
            outBuf.writeBytes(raw);
            out.add(outBuf);
            this.checkpoint(STATE.READ_MAGIC_NUMBER);
            break;
        }
        default:
            throw new Error("Shouldn't reach here.");
        }


    }

}
