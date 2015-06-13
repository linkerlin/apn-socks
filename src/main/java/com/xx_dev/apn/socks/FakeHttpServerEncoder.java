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
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.FakeHttpServerEncoder 2015-06-13 19:16 (xmx) Exp $
 */
public class FakeHttpServerEncoder extends MessageToByteEncoder<ByteBuf> {

    private byte key = 0x23;

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf msg, final ByteBuf out) throws Exception {
        int length = msg.readableBytes();


        out.writeBytes(TextUtil.toUTF8Bytes("HTTP/1.1 200 OK \r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("Content-Type: image/png\r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("X-C: " + String.format("%1$08x", length) + "\r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("Content-Length: " +"1234" + "\r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("Connection: Keep-Alive\r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("Server: nginx\r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("\r\n"));


        byte[] buf = new byte[msg.readableBytes()];
        msg.readBytes(buf);

        byte[] res = new byte[msg.readableBytes()];

        for (int i=0; i< buf.length; i++ ) {
            res[i] = (byte)(buf[i] ^ key);
        }

        out.writeBytes(res);
    }
}
