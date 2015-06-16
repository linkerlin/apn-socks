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

package com.xx_dev.apn.socks.remote;

import com.xx_dev.apn.socks.common.utils.TextUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.ApnProxyAESDecoder 14-6-28 12:09 (xmx) Exp $
 */
public class FakeHttpServerDecoder extends ReplayingDecoder<FakeHttpServerDecoder.STATE> {

    private static final Logger logger = Logger.getLogger(FakeHttpServerDecoder.class);

    enum STATE {
        READ_SKIP_1,
        READ_LENGTH,
        READ_SKIP_2,
        READ_CONTENT
    }

    private int length;


    public FakeHttpServerDecoder() {
        super(STATE.READ_SKIP_1);


    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (this.state()) {
        case READ_SKIP_1: {
            in.skipBytes(54);
            this.checkpoint(STATE.READ_LENGTH);
        }
        case READ_LENGTH: {
            byte[] buf = new byte[8];
            in.readBytes(buf);

            String s = TextUtil.fromUTF8Bytes(buf);


            length = Integer.parseInt(s, 16);

            this.checkpoint(STATE.READ_SKIP_2);
        }
        case READ_SKIP_2: {
            in.skipBytes(50);
            this.checkpoint(STATE.READ_CONTENT);
        }
        case READ_CONTENT: {

            if (length > 0) {
                byte[] buf = new byte[length];
                in.readBytes(buf, 0, length);

                byte[] res = new byte[length];

                for (int i = 0; i < length; i++) {
                    res[i] = (byte) (buf[i] ^ (RemoteConfig.ins().getEncryptKey() & 0xFF));
                }

                ByteBuf outBuf = ctx.alloc().buffer();

                outBuf.writeBytes(res);

                out.add(outBuf);
            }

            this.checkpoint(STATE.READ_SKIP_1);
            break;
        }
        default:
            throw new Error("Shouldn't reach here.");
        }
    }

}
