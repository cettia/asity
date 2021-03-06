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
package io.cettia.asity.bridge.grizzly2;

import io.cettia.asity.websocket.AbstractServerWebSocket;
import io.cettia.asity.websocket.ServerWebSocket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketAdapter;

import javax.servlet.http.HttpServletRequest;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link ServerWebSocket} for Grizzly 2.
 *
 * @author Donghwan Kim
 */
public class GrizzlyServerWebSocket extends AbstractServerWebSocket {

  // To access the upgrade request for this WebSocket
  private final DefaultWebSocket socket;

  public GrizzlyServerWebSocket(DefaultWebSocket webSocket) {
    this.socket = webSocket;
    socket.add(new WebSocketAdapter() {
      @Override
      public void onMessage(WebSocket socket, String message) {
        textActions.fire(message);
      }

      @Override
      public void onMessage(WebSocket socket, byte[] bytes) {
        binaryActions.fire(ByteBuffer.wrap(bytes));
      }

      @Override
      public void onClose(WebSocket socket, DataFrame frame) {
        closeActions.fire();
      }
    });
  }

  void onError(Throwable e) {
    errorActions.fire(e);
  }

  @Override
  public String uri() {
    HttpServletRequest request = socket.getUpgradeRequest();
    String uri = request.getRequestURI();
    if (request.getQueryString() != null) {
      uri += "?" + request.getQueryString();
    }
    return uri;
  }

  @Override
  public Set<String> headerNames() {
    return new LinkedHashSet(Collections.list(socket.getUpgradeRequest().getHeaderNames()));
  }

  @Override
  public List<String> headers(String name) {
    return Collections.list(socket.getUpgradeRequest().getHeaders(name));
  }

  @Override
  protected void doSend(String data) {
    socket.send(data);
  }

  @Override
  protected void doSend(ByteBuffer data) {
    byte[] bytes = new byte[data.remaining()];
    data.get(bytes);
    socket.send(bytes);
  }

  @Override
  protected void doClose() {
    socket.close();
  }

  @Override
  public <T> T unwrap(Class<T> clazz) {
    return WebSocket.class.isAssignableFrom(clazz) ? clazz.cast(socket) : null;
  }

}
