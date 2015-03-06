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
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

import java.util.List;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.common.ForwardMsgDecoder 2015-03-06 19:48 (xmx) Exp $
 */
public class ForwardMsgDecoder extends ReplayingDecoder<ForwardMsgDecoder.STATE> {
    private static final String name = "FORWARD_MSG_DECODER";

    /**
     * @deprecated Will be removed at the next minor version bump.
     */
    @Deprecated
    public static String getName() {
        return name;
    }

    private int type;
    private int streamId;
    private SocksAddressType addressType;
    private int fieldLength;
    private String host;
    private int port;
    private SocksCmdStatus cmdStatus;
    private ForwardMsg msg;

    enum STATE {
        READ_MSG_TYPE,
        READ_STREAM_ID,
        READ_RELAY_DATA,
        READ_CMD_ADDRESS_TYPE,
        READ_CMD_ADDRESS,
        READ_CMD_STATUS
    }

    public ForwardMsgDecoder() {
        super(STATE.READ_MSG_TYPE);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {
        switch (state()) {
        case READ_MSG_TYPE: {
            type = byteBuf.readShort();
            checkpoint(STATE.READ_STREAM_ID);
        }
        default:break;
        }

        switch (type) {
        case 0: {
            switch (state()) {
            case READ_STREAM_ID: {
                streamId = byteBuf.readShort();
                checkpoint(STATE.READ_RELAY_DATA);
            }
            case READ_RELAY_DATA: {
                int relayLength = byteBuf.readByte();
                ByteBuf readBytes = byteBuf.readBytes(relayLength);
                msg = new ForwardRelayMsg(streamId, readBytes);
            }
            default: {
                checkpoint(STATE.READ_MSG_TYPE);
                break;
            }
            }

        }
        case 1: {
            switch (state()) {
            case READ_STREAM_ID: {
                streamId = byteBuf.readShort();
                checkpoint(STATE.READ_CMD_ADDRESS_TYPE);
            }
            case READ_CMD_ADDRESS_TYPE: {
                addressType = SocksAddressType.valueOf(byteBuf.readByte());
                checkpoint(STATE.READ_CMD_ADDRESS);
            }
            case READ_CMD_ADDRESS: {
                switch (addressType) {
                case IPv4: {
                    host = intToIp(byteBuf.readInt());
                    port = byteBuf.readUnsignedShort();
                    msg = new ForwardRequest(streamId, addressType, host, port);
                    break;
                }
                case DOMAIN: {
                    fieldLength = byteBuf.readByte();
                    host = byteBuf.readBytes(fieldLength).toString(CharsetUtil.US_ASCII);
                    port = byteBuf.readUnsignedShort();
                    msg = new ForwardRequest(streamId, addressType, host, port);
                    break;
                }
                case IPv6: {
                    host = ipv6toStr(byteBuf.readBytes(16).array());
                    port = byteBuf.readUnsignedShort();
                    msg = new ForwardRequest(streamId, addressType, host, port);
                    break;
                }
                case UNKNOWN:
                    break;
                }
            }
            default: {
                checkpoint(STATE.READ_MSG_TYPE);
                break;
            }
            }
        }
        case 2: {
            switch (state()) {
            case READ_STREAM_ID: {
                streamId = byteBuf.readShort();
                checkpoint(STATE.READ_CMD_STATUS);
            }
            case READ_CMD_STATUS: {
                cmdStatus = SocksCmdStatus.valueOf(byteBuf.readByte());
                msg = new ForwardResponse(streamId, cmdStatus);
            }
            default: {
                checkpoint(STATE.READ_MSG_TYPE);
                break;
            }
            }
        }
        }
        out.add(msg);
    }

    private static final int SECOND_ADDRESS_OCTET_SHIFT = 16;
    private static final int FIRST_ADDRESS_OCTET_SHIFT = 24;
    private static final int THIRD_ADDRESS_OCTET_SHIFT = 8;
    private static final int XOR_DEFAULT_VALUE = 0xff;

    public static String intToIp(int i) {
        return String.valueOf(i >> FIRST_ADDRESS_OCTET_SHIFT & XOR_DEFAULT_VALUE) + '.' +
               (i >> SECOND_ADDRESS_OCTET_SHIFT & XOR_DEFAULT_VALUE) + '.' +
               (i >> THIRD_ADDRESS_OCTET_SHIFT & XOR_DEFAULT_VALUE) + '.' +
               (i & XOR_DEFAULT_VALUE);
    }

    /**
     * Converts numeric IPv6 to standard (non-compressed) format.
     */
    public static String ipv6toStr(byte[] src) {
        assert src.length == 16;
        StringBuilder sb = new StringBuilder(39);
        ipv6toStr(sb, src, 0, 8);
        return sb.toString();
    }

    private static void ipv6toStr(StringBuilder sb, byte[] src, int fromHextet, int toHextet) {
        int i;
        toHextet --;
        for (i = fromHextet; i < toHextet; i++) {
            appendHextet(sb, src, i);
            sb.append(':');
        }

        appendHextet(sb, src, i);
    }

    private static void appendHextet(StringBuilder sb, byte[] src, int i) {
        StringUtil.toHexString(sb, src, i << 1, 2);
    }

}
