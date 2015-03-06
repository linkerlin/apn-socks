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

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.common.ForwardMsg 2015-03-05 19:13 (xmx) Exp $
 */
public abstract class ForwardMsg {

    private final int type;
    private final int streamId;

    ForwardMsg(int type, int streamId) {
        this.type = type;
        this.streamId = streamId;
    }

    /**
     * Returns the type of this {@link ForwardMsg}
     *
     * @return The type of this {@link ForwardMsg}
     */
    public int type() {
        return type;
    }

    /**
     * Returns the streamId of this {@link ForwardMsg}
     *
     * @return The streamId of this {@link ForwardMsg}
     */
    public int streamId() {
        return streamId;
    }

    public abstract void encodeAsByteBuf(ByteBuf byteBuf);
}
