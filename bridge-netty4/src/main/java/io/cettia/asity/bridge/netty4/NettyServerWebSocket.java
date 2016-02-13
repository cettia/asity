/*
 * Copyright 2015 the original author or authors.
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

import io.cettia.asity.websocket.AbstractServerWebSocket;
import io.cettia.asity.websocket.ServerWebSocket;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

import java.nio.ByteBuffer;

/**
 * {@link ServerWebSocket} for Netty 4.
 *
 * @author Donghwan Kim
 */
public class NettyServerWebSocket extends AbstractServerWebSocket {

  private final ChannelHandlerContext context;
  private final FullHttpRequest request;
  private final WebSocketServerHandshaker handshaker;

  public NettyServerWebSocket(ChannelHandlerContext context, FullHttpRequest req,
                              WebSocketServerHandshaker handshaker) {
    this.context = context;
    this.request = req;
    this.handshaker = handshaker;
  }

  void handleFrame(WebSocketFrame frame) {
    if (frame instanceof TextWebSocketFrame) {
      textActions.fire(((TextWebSocketFrame) frame).text());
    } else if (frame instanceof BinaryWebSocketFrame) {
      binaryActions.fire(frame.content().nioBuffer());
    } else if (frame instanceof CloseWebSocketFrame) {
      handshaker.close(context.channel(), (CloseWebSocketFrame) frame.retain());
      closeActions.fire();
    }
  }

  void handleError(Throwable e) {
    errorActions.fire(e);
  }

  void handleClose() {
    closeActions.fire();
  }

  @Override
  public String uri() {
    return request.getUri();
  }

  @Override
  protected void doSend(ByteBuffer byteBuffer) {
    context.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(byteBuffer)));
  }

  @Override
  protected void doSend(String data) {
    context.writeAndFlush(new TextWebSocketFrame(data));
  }

  @Override
  protected void doClose() {
    context.close();
  }

  @Override
  public <T> T unwrap(Class<T> clazz) {
    return ChannelHandlerContext.class.isAssignableFrom(clazz) ?
      clazz.cast(context) :
      WebSocketServerHandshaker.class.isAssignableFrom(clazz) ?
        clazz.cast(handshaker) :
        FullHttpRequest.class.isAssignableFrom(clazz) ?
          clazz.cast(request) :
          null;
  }

}
