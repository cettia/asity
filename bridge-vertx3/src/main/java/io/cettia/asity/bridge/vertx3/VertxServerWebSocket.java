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
package io.cettia.asity.bridge.vertx3;

import io.cettia.asity.websocket.AbstractServerWebSocket;
import io.cettia.asity.websocket.ServerWebSocket;
import io.vertx.core.buffer.Buffer;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * {@link ServerWebSocket} for Vert.x 3.
 *
 * @author Donghwan Kim
 */
public class VertxServerWebSocket extends AbstractServerWebSocket {

  private final io.vertx.core.http.ServerWebSocket socket;

  public VertxServerWebSocket(io.vertx.core.http.ServerWebSocket socket) {
    this.socket = socket;
    socket.closeHandler(closeActions::fire).exceptionHandler(errorActions::fire).frameHandler(frame -> {
      // Deal with only text and binary frames
      if (frame.isText()) {
        textActions.fire(frame.textData());
     } else if (frame.isBinary()) {
        binaryActions.fire(frame.binaryData().getByteBuf().nioBuffer());
      }
    });
  }

  @Override
  public String uri() {
    return socket.uri();
  }

  @Override
  public Set<String> headerNames() {
    return socket.headers().names();
  }

  @Override
  public List<String> headers(String name) {
    return socket.headers().getAll(name);
  }

  @Override
  protected void doClose() {
    socket.close();
  }

  @Override
  protected void doSend(String data) {
    socket.writeFinalTextFrame(data);
  }

  @Override
  protected void doSend(ByteBuffer byteBuffer) {
    socket.writeFinalBinaryFrame(Buffer.buffer().setBytes(0, byteBuffer));
  }

  /**
   * {@link io.vertx.core.http.ServerWebSocket} is available.
   */
  @Override
  public <T> T unwrap(Class<T> clazz) {
    return io.vertx.core.http.ServerWebSocket.class.isAssignableFrom(clazz) ? clazz.cast(socket) : null;
  }

}
