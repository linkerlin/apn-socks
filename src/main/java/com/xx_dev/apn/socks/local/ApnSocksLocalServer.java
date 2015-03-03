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

import com.xx_dev.apn.socks.util.LoggerUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.log4j.Logger;

public final class ApnSocksLocalServer {

    private static final Logger logger = Logger.getLogger(ApnSocksLocalServer.class);

    private static final int PORT = Integer.parseInt(System.getProperty("port", "8888"));

    private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public void start() {
        LoggerUtil.info(logger, "ApnSocks Local Server Starting on 8866");
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
             .handler(new LoggingHandler("NET_LOGGER", LogLevel.DEBUG))
             .childHandler(new ApnSocksLocalServerInitializer());
            b.bind(PORT).sync().channel().closeFuture().sync();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void shutdown() {
        LoggerUtil.info(logger, "ApnSocks Local Server Shutdown");
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
