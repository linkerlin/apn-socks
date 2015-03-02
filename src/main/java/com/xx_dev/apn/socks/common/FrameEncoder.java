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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.log4j.Logger;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.common.FrameEncoder 2015-02-28 18:12 (xmx) Exp $
 */
public class FrameEncoder extends MessageToByteEncoder<ByteBuf> {
    private static final Logger logger = Logger.getLogger(FrameEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        try {

            while(msg.readableBytes() > 0) {
                int readLength = msg.readableBytes();
                if (readLength > 1024*512) {
                    readLength = 1024*512;
                }

                byte[] array = new byte[readLength];
                msg.readBytes(array, 0, readLength);

                byte[] raw = array; // todo use orig bytes for mock
                int length = raw.length;

                out.writeInt(0x34ed2b11);//magic number
                out.writeInt(length);
                out.writeBytes(raw);
            }


        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }
}
