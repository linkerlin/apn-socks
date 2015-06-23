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
import com.xx_dev.apn.socks.local.LocalConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;


public class FakeHttpServerDecoder extends ReplayingDecoder<FakeHttpServerDecoder.STATE> {

    private static final Logger logger = Logger.getLogger(FakeHttpServerDecoder.class);

    private static final Logger perfLogger = Logger.getLogger("PERF_LOGGER");

    private static final Logger trafficLogger = Logger.getLogger("TRAFFIC_LOGGER");

    enum STATE {
        READ_FAKE_HTTP,
        READ_CONTENT
    }

    private int length;


    public FakeHttpServerDecoder() {
        super(STATE.READ_FAKE_HTTP);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int startReaderIndex = in.readerIndex();

        long start = System.nanoTime();
        this._decode(ctx, in, out);
        long end = System.nanoTime();

        int endReaderIndex = in.readerIndex();

        perfLogger.debug("remote decode: " + (endReaderIndex - startReaderIndex) + ", " + (end - start));

    }

    protected void _decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        switch (this.state()) {
        case READ_FAKE_HTTP: {
            int fakeHttpHeadStartIndex = in.readerIndex();

            int fakeHttpHeadEndIndex = in.forEachByte(new ByteBufProcessor() {
                int c = 0;

                @Override
                public boolean process(byte value) throws Exception {

                    if (value == '\r' || value == '\n') {
                        c++;
                    } else {
                        c = 0;
                    }

                    //logger.info("value=" + value + ", c=" + c);

                    if (c >= 4) {
                        return false;
                    } else {
                        return true;
                    }
                }
            });

            logger.debug("s: " + fakeHttpHeadStartIndex);
            logger.debug("e: " + fakeHttpHeadEndIndex);

            if (fakeHttpHeadEndIndex == -1) {
                logger.warn("w: " + fakeHttpHeadStartIndex);
                break;
            }

            byte[] buf = new byte[fakeHttpHeadEndIndex - fakeHttpHeadStartIndex + 1];
            in.readBytes(buf, 0, fakeHttpHeadEndIndex - fakeHttpHeadStartIndex + 1);
            String s = TextUtil.fromUTF8Bytes(buf);

            //logger.info(s);

            String[] ss = StringUtils.split(s, "\r\n");

            //System.out.println(s + "" + this + " " + Thread.currentThread().getName());


            for (String line : ss) {
                if (StringUtils.startsWith(line, "X-C:")) {
                    String lenStr = StringUtils.trim(StringUtils.split(line, ":")[1]);
                    //System.out.println(lenStr + "" + this + " " + Thread.currentThread().getName());
                    //System.out.println("*****************************************");
                    try {
                        length = Integer.parseInt(lenStr, 16);
                    } catch (Throwable t) {
                        logger.error("--------------------------------------");
                        logger.error(s + "" + this + " " + Thread.currentThread().getName());
                        logger.error("--------------------------------------");
                    }

                }

                if (StringUtils.startsWith(line, "X-U:")) {
                    String user = StringUtils.trim(StringUtils.split(line, ":")[1]);
                    ctx.attr(NettyAttributeKey.LINK_USER).set(user);
                    logger.info(user);
                }
            }

            this.checkpoint(STATE.READ_CONTENT);
        }
        case READ_CONTENT: {
            if (length > 0) {
                trafficLogger.info("U," + ctx.attr(NettyAttributeKey.LINK_USER).get()+"," +length);

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

            this.checkpoint(STATE.READ_FAKE_HTTP);
            break;
        }
        default:
            throw new Error("Shouldn't reach here.");
        }


    }
}
