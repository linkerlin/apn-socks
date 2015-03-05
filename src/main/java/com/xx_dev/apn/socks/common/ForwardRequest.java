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
import io.netty.handler.codec.socks.SocksCmdType;
import io.netty.handler.codec.socks.SocksRequestType;
import io.netty.util.CharsetUtil;
import io.netty.util.NetUtil;

import java.net.IDN;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.common.ForwardRequest 2015-03-05 15:32 (xmx) Exp $
 */
public class ForwardRequest extends ForwardMsg{

    private final int streamId;
    private final SocksAddressType addressType;
    private final String host;
    private final int port;

    public ForwardRequest(int streamId, SocksAddressType addressType, String host, int port) {
        if (addressType == null) {
            throw new NullPointerException("addressType");
        }
        if (host == null) {
            throw new NullPointerException("host");
        }
        switch (addressType) {
        case IPv4:
            if (!NetUtil.isValidIpV4Address(host)) {
                throw new IllegalArgumentException(host + " is not a valid IPv4 address");
            }
            break;
        case DOMAIN:
            if (IDN.toASCII(host).length() > 255) {
                throw new IllegalArgumentException(host + " IDN: " + IDN.toASCII(host) + " exceeds 255 char limit");
            }
            break;
        case IPv6:
            if (!NetUtil.isValidIpV6Address(host)) {
                throw new IllegalArgumentException(host + " is not a valid IPv6 address");
            }
            break;
        case UNKNOWN:
            break;
        }

        if (streamId <=0 || streamId >= 65536) {
            throw new IllegalArgumentException(streamId + " is not in bounds 0 < x < 65536");
        }

        if (port <= 0 || port >= 65536) {
            throw new IllegalArgumentException(port + " is not in bounds 0 < x < 65536");
        }
        this.streamId = streamId;
        this.addressType = addressType;
        this.host = IDN.toASCII(host);
        this.port = port;
    }

    /**
     * Returns the streamId of this {@link ForwardRequest}
     *
     * @return The streamId of this {@link ForwardRequest}
     */
    public int streamId() {
        return streamId;
    }

    /**
     * Returns the {@link SocksAddressType} of this {@link ForwardRequest}
     *
     * @return The {@link SocksAddressType} of this {@link ForwardRequest}
     */
    public SocksAddressType addressType() {
        return addressType;
    }

    /**
     * Returns host that is used as a parameter in {@link SocksCmdType}
     *
     * @return host that is used as a parameter in {@link SocksCmdType}
     */
    public String host() {
        return IDN.toUnicode(host);
    }

    /**
     * Returns port that is used as a parameter in {@link SocksCmdType}
     *
     * @return port that is used as a parameter in {@link SocksCmdType}
     */
    public int port() {
        return port;
    }

    public void encodeAsByteBuf(ByteBuf byteBuf) {
        byteBuf.writeShort(streamId);
        byteBuf.writeByte(addressType.byteValue());
        switch (addressType) {
        case IPv4: {
            byteBuf.writeBytes(NetUtil.createByteArrayFromIpAddressString(host));
            byteBuf.writeShort(port);
            break;
        }

        case DOMAIN: {
            byteBuf.writeByte(host.length());
            byteBuf.writeBytes(host.getBytes(CharsetUtil.US_ASCII));
            byteBuf.writeShort(port);
            break;
        }

        case IPv6: {
            byteBuf.writeBytes(NetUtil.createByteArrayFromIpAddressString(host));
            byteBuf.writeShort(port);
            break;
        }
        }
    }


}
