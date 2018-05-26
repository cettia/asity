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

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.websocket.ServerWebSocket;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocketHandler to provide {@link SpringWebMvcServerWebSocket}.
 * <p/>
 * <pre>
 *{@literal @}Bean
 * public AsityWebSocketHandler webSocketHandler() {
 *   return new AsityWebSocketHandler();
 * }
 *
 *{@literal @}Override // A contract from WebSocketConfigurer
 * public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
 *   AsityWebSocketHandler webSocketHandler = new AsityWebSocketHandler().onwebsocket(ws -&gt; {});
 *   registry.addHandler(webSocketHandler, "/cettia");
 * }
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityWebSocketHandler extends AbstractWebSocketHandler {

  private Actions<ServerWebSocket> wsActions = new ConcurrentActions<>();
  private Map<String, SpringWebMvcServerWebSocket> sessions = new ConcurrentHashMap<>();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    SpringWebMvcServerWebSocket ws = new SpringWebMvcServerWebSocket(session);
    sessions.put(session.getId(), ws);
    wsActions.fire(ws);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    SpringWebMvcServerWebSocket ws = sessions.remove(session.getId());
    ws.onClose();
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    SpringWebMvcServerWebSocket ws = sessions.get(session.getId());
    ws.onError(exception);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    SpringWebMvcServerWebSocket ws = sessions.get(session.getId());
    ws.onTextMessage(message.getPayload());
  }

  @Override
  protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
    SpringWebMvcServerWebSocket ws = sessions.get(session.getId());
    ws.onBinaryMessage(message.getPayload());
  }

  /**
   * Registers an action to be called when {@link ServerWebSocket} is available.
   */
  public AsityWebSocketHandler onwebsocket(Action<ServerWebSocket> action) {
    wsActions.add(action);
    return this;
  }

}
