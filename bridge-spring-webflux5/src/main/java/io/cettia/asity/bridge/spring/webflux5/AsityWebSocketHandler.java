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
package io.cettia.asity.bridge.spring.webflux5;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.websocket.ServerWebSocket;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 * WebSocketHandler to provide {@link SpringWebFluxServerWebSocket}.
 * <p/>
 * <pre>
 *{@literal @}Bean
 * public AsityWebSocketHandler webSocketHandler() {
 *   return new AsityWebSocketHandler().onwebsocket(ws -&gt; {});
 * }
 *
 *{@literal @}Bean
 * public HandlerMapping wsMapping() {
 *   AsityWebSocketHandler webSocketHandler = new AsityWebSocketHandler();
 *   Map&lt;String, WebSocketHandler&gt; map = new LinkedHashMap&lt;&gt;();
 *   map.put("/cettia", webSocketHandler());
 *
 *   SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
 *   mapping.setUrlMap(map);
 *   return mapping;
 * }
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityWebSocketHandler implements WebSocketHandler {

  private Actions<ServerWebSocket> wsActions = new ConcurrentActions<>();

  @Override
  public Mono<Void> handle(WebSocketSession session) {
    SpringWebFluxServerWebSocket ws = new SpringWebFluxServerWebSocket(session);
    return ws.getMono().doOnSubscribe($ -> wsActions.fire(ws));
  }

  /**
   * Registers an action to be called when {@link ServerWebSocket} is available.
   */
  public AsityWebSocketHandler onwebsocket(Action<ServerWebSocket> action) {
    wsActions.add(action);
    return this;
  }

}
