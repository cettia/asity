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
package io.cettia.asity.bridge.jwa1;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.websocket.ServerWebSocket;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
 * Endpoint to process {@link Endpoint} and {@link Session} into {@link ServerWebSocket}.
 * <p/>
 * <p/>
 * <pre>
 * ServerEndpointConfig config = ServerEndpointConfig.Builder.create(AsityServerEndpoint.class,
 * "/cettia")
 * .configurator(new Configurator() {
 *     {@literal @}Override
 *     protected &lt;T&gt; T getEndpointInstance(Class&lt;T&gt; endpointClass) {
 *         return endpointClass.cast(new AsityServerEndpoint().onwebsocket(ws -&gt; {}));
 *     }
 *
 *     {@literal @}Override
 *     public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
 *       config.getUserProperties().put(HandshakeRequest.class.getName(), request);
 *     }
 * })
 * .build();
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityServerEndpoint extends Endpoint {

  private Actions<ServerWebSocket> wsActions = new ConcurrentActions<>();
  private JwaServerWebSocket ws;

  @Override
  public void onOpen(Session session, EndpointConfig config) {
    ws = new JwaServerWebSocket(session);
    wsActions.fire(ws);
  }

  @Override
  public void onError(Session session, Throwable throwable) {
    ws.onError(throwable);
  }

  @Override
  public void onClose(Session session, CloseReason closeReason) {
    ws.onClose();
  }

  /**
   * Registers an action to be called when {@link ServerWebSocket} is
   * available.
   */
  public AsityServerEndpoint onwebsocket(Action<ServerWebSocket> action) {
    wsActions.add(action);
    return this;
  }

}
