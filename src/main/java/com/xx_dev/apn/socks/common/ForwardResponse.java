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
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.handler.codec.socks.SocksResponseType;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;

import java.net.IDN;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.common.ForwardResponse 2015-03-05 15:43 (xmx) Exp $
 */
public class ForwardResponse extends  ForwardMsg{

    private final SocksCmdStatus cmdStatus;


    /**
     * Constructs new response and includes provided host and port as part of it.
     *
     * @param streamId
     * @param cmdStatus status of the response
     * @throws NullPointerException in case cmdStatus or addressType are missing
     * @throws IllegalArgumentException in case host or port cannot be validated
     * @see java.net.IDN#toASCII(String)
     */
    public ForwardResponse(int streamId, SocksCmdStatus cmdStatus) {
        super(2, streamId);
        if (streamId <=0 || streamId >= 65536) {
            throw new IllegalArgumentException(streamId + " is not in bounds 0 < x < 65536");
        }

        if (cmdStatus == null) {
            throw new NullPointerException("cmdStatus");
        }

        this.cmdStatus = cmdStatus;
    }


    /**
     * Returns the {@link SocksCmdStatus} of this {@link ForwardResponse}
     *
     * @return The {@link SocksCmdStatus} of this {@link ForwardResponse}
     */
    public SocksCmdStatus cmdStatus() {
        return cmdStatus;
    }

    public void encodeAsByteBuf(ByteBuf byteBuf) {
        byteBuf.writeShort(this.type());
        byteBuf.writeShort(this.streamId());
        byteBuf.writeByte(cmdStatus.byteValue());
    }
}
