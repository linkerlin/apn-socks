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

import org.apache.log4j.xml.DOMConfigurator;

import javax.xml.parsers.FactoryConfigurationError;
import java.io.File;
import java.net.MalformedURLException;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.socks.remote.ApnSocksRemoteServerLauncher 2015-03-01 13:42 (xmx) Exp $
 */
public class ApnSocksRemoteServerLauncher {

    static {
        File log4jConfigFile = new File("conf/log4j.xml");
        if (log4jConfigFile.exists()) {
            try {
                DOMConfigurator.configure(log4jConfigFile.toURI().toURL());
            } catch (MalformedURLException e) {
                System.err.println(e);
            } catch (FactoryConfigurationError e) {
                System.err.println(e);
            }
        }
    }

    public static void main(String[] args) {
        ApnSocksRemoteServer server = new ApnSocksRemoteServer();
        server.start();
    }

}
