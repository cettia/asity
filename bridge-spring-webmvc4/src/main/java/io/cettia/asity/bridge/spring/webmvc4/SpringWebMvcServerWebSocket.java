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
package io.cettia.asity.bridge.spring.webmvc4;

import io.cettia.asity.websocket.AbstractServerWebSocket;
import io.cettia.asity.websocket.ServerWebSocket;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * {@link ServerWebSocket} for Spring Web MVC 4.
 *
 * @author Donghwan Kim
 */
public class SpringWebMvcServerWebSocket extends AbstractServerWebSocket {

  private final WebSocketSession session;

  public SpringWebMvcServerWebSocket(WebSocketSession session) {
    this.session = session;
  }

  void onClose() {
    closeActions.fire();
  }

  void onError(Throwable e) {
    errorActions.fire(e);
  }

  void onTextMessage(String textMessage) {
    textActions.fire(textMessage);
  }

  void onBinaryMessage(ByteBuffer binaryMessage) {
    binaryActions.fire(binaryMessage);
  }

  @Override
  public String uri() {
    URI uri = session.getUri();
    return uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
  }

  @Override
  public Set<String> headerNames() {
    return session.getHandshakeHeaders().keySet();
  }

  @Override
  public List<String> headers(String name) {
    return session.getHandshakeHeaders().get(name);
  }

  @Override
  protected void doSend(ByteBuffer byteBuffer) {
    try {
      session.sendMessage(new BinaryMessage(byteBuffer));
    } catch (IOException e) {
      errorActions.fire(e);
    }
  }

  @Override
  protected void doSend(String data) {
    try {
      session.sendMessage(new TextMessage(data));
    } catch (IOException e) {
      errorActions.fire(e);
    }
  }

  @Override
  protected void doClose() {
    try {
      session.close();
    } catch (IOException e) {
      errorActions.fire(e);
    }
  }

  /**
   * {@link WebSocketSession} is available.
   */
  @Override
  public <T> T unwrap(Class<T> clazz) {
    return WebSocketSession.class.isAssignableFrom(clazz) ? clazz.cast(session) : null;
  }

}
