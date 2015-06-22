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

import com.xx_dev.apn.socks.common.utils.TextUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class FakeHttpClientEncoder extends MessageToByteEncoder<ByteBuf> {

    @Override
    protected void encode(final ChannelHandlerContext ctx, final ByteBuf msg, final ByteBuf out) throws Exception {
        int length = msg.readableBytes();

        out.writeBytes(TextUtil.toUTF8Bytes("POST /form.action HTTP/1.1\r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("HOST: www.baidu.com\r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("X-C: " + String.format("%1$08x", length) + "\r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("X-U: " + "APN-SOCKS" + "\r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("Content-Length: " + "1234" + "\r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("Connection: Keep-Alive\r\n"));
        out.writeBytes(TextUtil.toUTF8Bytes("\r\n"));


        if (length > 0) {
            byte[] buf = new byte[length];
            msg.readBytes(buf, 0, length);

            byte[] res = new byte[length];

            for (int i = 0; i < buf.length; i++) {
                //res[i] = (byte)(buf[i] ^ key);
                res[i] = (byte) (buf[i] ^ (LocalConfig.ins().getEncryptKey() & 0xFF));
            }

            out.writeBytes(res);
        }

    }
}
