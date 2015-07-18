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
package io.cettia.asity.bridge.netty4;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.netty4.AsityServerCodec;
import io.cettia.asity.test.ServerWebSocketTest;
import io.cettia.asity.websocket.ServerWebSocket;
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
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.URI;

import org.junit.Test;

public class NettyServerWebSocketTest extends ServerWebSocketTest {

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
                .addLast(new AsityServerCodec() {
                    @Override
                    protected boolean accept(HttpRequest req) {
                        return URI.create(req.getUri()).getPath().equals("/test");
                    }
                }
                .onwebsocket(performer.serverAction()));
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
        performer.onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                assertThat(ws.unwrap(ChannelHandlerContext.class), instanceOf(ChannelHandlerContext.class));
                assertThat(ws.unwrap(WebSocketServerHandshaker.class), instanceOf(WebSocketServerHandshaker.class));
                assertThat(ws.unwrap(FullHttpRequest.class), instanceOf(FullHttpRequest.class));
                performer.start();
            }
        })
        .connect();
    }

}
