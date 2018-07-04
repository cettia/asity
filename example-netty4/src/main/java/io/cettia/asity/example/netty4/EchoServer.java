/*
 * Copyright 2018 the original author or authors.
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
package io.cettia.asity.example.netty4;

import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.netty4.AsityServerCodec;
import io.cettia.asity.example.echo.HttpEchoServer;
import io.cettia.asity.example.echo.WebSocketEchoServer;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.websocket.ServerWebSocket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;

import java.net.URI;

public class EchoServer {
  public static void main(String[] args) throws Exception {
    // Web fragments
    Action<ServerHttpExchange> httpAction = new HttpEchoServer();
    Action<ServerWebSocket> wsAction = new WebSocketEchoServer();

    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(bossGroup, workerGroup)
        .channel(NioServerSocketChannel.class)
        .childHandler(new ChannelInitializer<SocketChannel>() {
          @Override
          public void initChannel(SocketChannel ch) {
            AsityServerCodec asityServerCodec = new AsityServerCodec() {
              @Override
              protected boolean accept(HttpRequest req) {
                return URI.create(req.uri()).getPath().equals("/echo");
              }
            };
            asityServerCodec.onhttp(httpAction).onwebsocket(wsAction);

            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new HttpServerCodec()).addLast(asityServerCodec);
          }
        });
      Channel channel = bootstrap.bind(8080).sync().channel();
      channel.closeFuture().sync();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }
}
