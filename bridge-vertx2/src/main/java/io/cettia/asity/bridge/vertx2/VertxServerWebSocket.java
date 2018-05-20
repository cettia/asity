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
package io.cettia.asity.bridge.vertx2;

import io.cettia.asity.websocket.AbstractServerWebSocket;
import io.cettia.asity.websocket.ServerWebSocket;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.impl.ws.WebSocketFrameInternal;

import java.nio.ByteBuffer;

/**
 * {@link ServerWebSocket} for Vert.x 2.
 *
 * @author Donghwan Kim
 */
public class VertxServerWebSocket extends AbstractServerWebSocket {

  private final org.vertx.java.core.http.ServerWebSocket socket;

  public VertxServerWebSocket(org.vertx.java.core.http.ServerWebSocket socket) {
    this.socket = socket;
    socket.closeHandler(new VoidHandler() {
      @Override
      protected void handle() {
        closeActions.fire();
      }
    })
    .exceptionHandler(errorActions::fire)
    .frameHandler(f -> {
      // Deal with only data frames
      WebSocketFrameInternal frame = (WebSocketFrameInternal) f;
      switch (frame.type()) {
        case TEXT:
          textActions.fire(frame.textData());
          break;
        case BINARY:
          binaryActions.fire(frame.getBinaryData().nioBuffer());
          break;
        default:
          break;
      }
    });
  }

  @Override
  public String uri() {
    return socket.uri();
  }

  @Override
  protected void doClose() {
    socket.close();
  }

  @Override
  protected void doSend(String data) {
    socket.writeTextFrame(data);
  }

  @Override
  protected void doSend(ByteBuffer byteBuffer) {
    socket.writeBinaryFrame(new Buffer().setBytes(0, byteBuffer));
  }

  /**
   * {@link org.vertx.java.core.http.ServerWebSocket} is available.
   */
  @Override
  public <T> T unwrap(Class<T> clazz) {
    return org.vertx.java.core.http.ServerWebSocket.class.isAssignableFrom(clazz) ?
      clazz.cast(socket) :
      null;
  }

}
