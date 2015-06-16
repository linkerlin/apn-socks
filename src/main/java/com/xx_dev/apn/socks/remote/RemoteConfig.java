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

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class RemoteConfig {

    private static final Logger logger = Logger.getLogger(RemoteConfig.class);

    private class OrderProperties extends Properties {

        private static final long serialVersionUID = -4627607243846121965L;

        private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();

        public Enumeration<Object> keys() {
            return Collections.<Object>enumeration(keys);
        }

        public Object put(Object key, Object value) {
            keys.add(key);
            return super.put(key, value);
        }

        public Set<Object> keySet() {
            return keys;
        }

        public Set<String> stringPropertyNames() {
            Set<String> set = new LinkedHashSet<String>();

            for (Object key : this.keys) {
                set.add((String) key);
            }

            return set;
        }

    }

    // default config
    private static final String LISTEN_PORT = "8889";
    private static final String ENCRYPT_KEY = "0xA2";


    private final Properties p = new OrderProperties();


    private static final String configFilePath = "conf/remote.properties";

    private static final RemoteConfig ins = new RemoteConfig();


    private RemoteConfig() {
        p.put("listenPort", LISTEN_PORT);
        p.put("encryptKey", ENCRYPT_KEY);

        File configFile = new File(configFilePath);

        try {
            p.load(new FileInputStream(configFilePath));
        } catch (FileNotFoundException e) {
            logger.warn("Local config not exsit, create default");
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            p.store(new FileOutputStream(configFile), "Gen By APN-SOCKS LocalConfig");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static RemoteConfig ins() {
        return ins;
    }

    public final int getListenPort() {
        return Integer.parseInt(p.getProperty("listenPort", LISTEN_PORT));
    }

    public final int getEncryptKey() {
        return Integer.parseInt(p.getProperty("encryptKey", ENCRYPT_KEY), 16);
    }

}
