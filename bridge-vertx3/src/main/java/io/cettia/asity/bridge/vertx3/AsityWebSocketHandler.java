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

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.websocket.ServerWebSocket;
import io.vertx.core.Handler;

/**
 * <code>Handler&lt;io.vertx.core.http.ServerWebSocket&gt;</code> to provide {@link VertxServerWebSocket}.
 * <p/>
 * <pre>
 * AsityWebSocketHandler websocketHandler = new AsityWebSocketHandler().onwebsocket(ws -&gt; {});
 * httpServer.websocketHandler(socket -> {
 *   if (socket.path().equals("/cettia")) {
 *     websocketHandler.handle(socket);
 *   }
 * });
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityWebSocketHandler implements Handler<io.vertx.core.http.ServerWebSocket> {

  private Actions<ServerWebSocket> wsActions = new ConcurrentActions<>();

  @Override
  public void handle(io.vertx.core.http.ServerWebSocket ws) {
    wsActions.fire(new VertxServerWebSocket(ws));
  }

  /**
   * Registers an action to be called when {@link ServerWebSocket} is available.
   */
  public AsityWebSocketHandler onwebsocket(Action<ServerWebSocket> action) {
    wsActions.add(action);
    return this;
  }

}
