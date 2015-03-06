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
import io.netty.util.ReferenceCounted;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.common.ForwardRelayMsg 2015-03-06 19:46 (xmx) Exp $
 */
public class ForwardRelayMsg extends ForwardMsg implements ReferenceCounted {
    private ByteBuf relayMsgByteBuf;

    public ForwardRelayMsg (int streamId, ByteBuf relayMsgByteBuf) {
        super(0, streamId);
        this.relayMsgByteBuf = relayMsgByteBuf;
    }

    /**
     * Returns the relayMsgByteBuf of this {@link ForwardMsg}
     *
     * @return The relayMsgByteBuf of this {@link ForwardMsg}
     */
    public ByteBuf relayMsgByteBuf() {
        return relayMsgByteBuf;
    }


    @Override
    public void encodeAsByteBuf(ByteBuf byteBuf) {
        while (relayMsgByteBuf.readableBytes() > 0) {
            byteBuf.writeByte(this.type());
            byteBuf.writeShort(this.streamId());

            int length = relayMsgByteBuf.readableBytes() > 1024?1024:relayMsgByteBuf.readableBytes();

            byteBuf.writeShort(length);
            byteBuf.writeBytes(relayMsgByteBuf, length);
        }
    }

    @Override
    public int refCnt() {
        return relayMsgByteBuf.refCnt();
    }

    @Override
    public ReferenceCounted retain() {
        return relayMsgByteBuf.retain();
    }

    @Override
    public ReferenceCounted retain(int increment) {
        return relayMsgByteBuf.retain(increment);
    }

    @Override
    public boolean release() {
        return relayMsgByteBuf.release();
    }

    @Override
    public boolean release(int decrement) {
        return relayMsgByteBuf.release(decrement);
    }
}
