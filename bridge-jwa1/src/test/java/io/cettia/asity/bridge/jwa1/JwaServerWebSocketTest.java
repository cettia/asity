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
import io.cettia.asity.test.ServerWebSocketTestBase;
import io.cettia.asity.websocket.ServerWebSocket;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.junit.Test;

import javax.websocket.HandshakeResponse;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.server.ServerEndpointConfig.Configurator;
import java.net.URI;

/**
 * @author Donghwan Kim
 */
public class JwaServerWebSocketTest extends ServerWebSocketTestBase {

  private Server server;

  @Override
  protected void startServer(int port, final Action<ServerWebSocket> websocketAction) throws
    Exception {
    server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.addConnector(connector);
    ServletContextHandler handler = new ServletContextHandler();
    server.setHandler(handler);
    ServerContainer container = WebSocketServerContainerInitializer.configureContext(handler);
    ServerEndpointConfig config = ServerEndpointConfig.Builder.create(AsityServerEndpoint.class,
      TEST_URI)
    .configurator(new Configurator() {
      @Override
      public <T> T getEndpointInstance(Class<T> endpointClass) {
        return endpointClass.cast(new AsityServerEndpoint().onwebsocket(websocketAction));
      }

      @Override
      public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        config.getUserProperties().put(HandshakeRequest.class.getName(), request);
      }
    })
    .build();
    container.addEndpoint(config);
    server.start();
  }

  @Test
  public void unwrap() throws Throwable {
    websocketAction(ws -> {
      threadAssertTrue(ws.unwrap(Session.class) instanceof Session);
      resume();
    });
    client.connect(new WebSocketAdapter(), URI.create(uri()));
    await();
  }

  @Override
  protected void stopServer() throws Exception {
    server.stop();
  }

}
