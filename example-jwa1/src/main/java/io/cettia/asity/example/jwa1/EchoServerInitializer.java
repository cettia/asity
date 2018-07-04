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
package io.cettia.asity.example.jwa1;

import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.jwa1.AsityServerEndpoint;
import io.cettia.asity.example.echo.WebSocketEchoServer;
import io.cettia.asity.websocket.ServerWebSocket;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.websocket.DeploymentException;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

@WebListener
public class EchoServerInitializer implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    // Web fragments
    Action<ServerWebSocket> wsAction = new WebSocketEchoServer();

    ServletContext context = event.getServletContext();
    ServerContainer container = (ServerContainer) context.getAttribute(ServerContainer.class.getName());
    ServerEndpointConfig.Configurator configurator = new ServerEndpointConfig.Configurator() {
      @Override
      public <T> T getEndpointInstance(Class<T> endpointClass) {
        AsityServerEndpoint asityServerEndpoint = new AsityServerEndpoint().onwebsocket(wsAction);
        return endpointClass.cast(asityServerEndpoint);
      }

      @Override
      public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        config.getUserProperties().put(HandshakeRequest.class.getName(), request);
      }
    };
    try {
      container.addEndpoint(ServerEndpointConfig.Builder.create(AsityServerEndpoint.class, "/echo").configurator(configurator).build());
    } catch (DeploymentException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
}