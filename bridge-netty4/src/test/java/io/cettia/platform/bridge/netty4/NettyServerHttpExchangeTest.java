/*
 * Copyright 2015 The Cettia Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cettia.platform.bridge.netty4;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import io.cettia.platform.action.Action;
import io.cettia.platform.http.ServerHttpExchange;
import io.cettia.platform.test.ServerHttpExchangeTest;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.URI;

import org.junit.Test;

public class NettyServerHttpExchangeTest extends ServerHttpExchangeTest {

    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
    ChannelGroup channels;

    @Override
    protected void startServer() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                channels.add(ctx.channel());
            }

            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast(new HttpServerCodec())
                .addLast(new CettiaServerCodec() {
                    @Override
                    protected boolean accept(HttpRequest req) {
                        return URI.create(req.getUri()).getPath().equals("/test");
                    }
                }
                .onhttp(performer.serverAction()));
            }
        });
        channels.add(bootstrap.bind(port).channel());
    }

    @Override
    protected void stopServer() {
        channels.close();
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    @Test
    public void unwrap() {
        performer.onserver(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                assertThat(http.unwrap(ChannelHandlerContext.class), instanceOf(ChannelHandlerContext.class));
                assertThat(http.unwrap(HttpRequest.class), instanceOf(HttpRequest.class));
                assertThat(http.unwrap(HttpResponse.class), instanceOf(HttpResponse.class));
                performer.start();
            }
        })
        .send();
    }

}
