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

package com.xx_dev.apn.socks.common.utils;

import java.io.UnsupportedEncodingException;

public class TextUtil {

    public static byte[] toUTF8Bytes(String src) {
        if (src == null) {
            return null;
        }

        byte[] bytes = null;

        try {
            bytes = src.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
        }

        return bytes;
    }

    public static String fromUTF8Bytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        String str = null;

        try {
            str = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }

        return str;

    }
}
