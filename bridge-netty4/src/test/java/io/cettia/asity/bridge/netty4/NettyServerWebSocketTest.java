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
package io.cettia.asity.bridge.netty4;

import io.cettia.asity.action.Action;
import io.cettia.asity.test.ServerWebSocketTestBase;
import io.cettia.asity.websocket.ServerWebSocket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.junit.Test;

import java.net.URI;

/**
 * @author Donghwan Kim
 */
public class NettyServerWebSocketTest extends ServerWebSocketTestBase {

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;

  @Override
  protected void startServer(int port, final Action<ServerWebSocket> websocketAction) throws
    Exception {
    bossGroup = new NioEventLoopGroup();
    workerGroup = new NioEventLoopGroup();

    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(bossGroup, workerGroup)
      .channel(NioServerSocketChannel.class)
      .childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel ch) throws Exception {
          ChannelPipeline pipeline = ch.pipeline();
          pipeline.addLast(new HttpServerCodec())
            .addLast(new AsityServerCodec() {
              @Override
              protected boolean accept(HttpRequest req) {
                return URI.create(req.getUri()).getPath().equals(TEST_PATH);
              }
            }.onwebsocket(websocketAction));
        }
      });
    bootstrap.bind(port).sync();
  }

  @Override
  protected void stopServer() {
    workerGroup.shutdownGracefully();
    bossGroup.shutdownGracefully();
  }

  @Test
  public void unwrap() throws Throwable {
    websocketAction(ws -> {
      threadAssertTrue(ws.unwrap(ChannelHandlerContext.class) instanceof ChannelHandlerContext);
      threadAssertTrue(ws.unwrap(WebSocketServerHandshaker.class) instanceof
        WebSocketServerHandshaker);
      threadAssertTrue(ws.unwrap(FullHttpRequest.class) instanceof FullHttpRequest);
      resume();
    });
    client.connect(new WebSocketAdapter(), URI.create(uri()));
    await();
  }

}
