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
package io.cettia.asity.bridge.vertx2;

import io.cettia.asity.action.Action;
import io.cettia.asity.test.ServerWebSocketTestBase;
import io.cettia.asity.websocket.ServerWebSocket;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServer;

import java.net.URI;

/**
 * @author Donghwan Kim
 */
public class VertxServerWebSocketTest extends ServerWebSocketTestBase {

  private HttpServer server;

  @Override
  protected void startServer(int port, Action<ServerWebSocket> websocketAction) {
    server = VertxFactory.newVertx().createHttpServer();
    final AsityWebSocketHandler websocketHandler = new AsityWebSocketHandler().onwebsocket(websocketAction);
    server.websocketHandler(new Handler<org.vertx.java.core.http.ServerWebSocket>() {
      @Override
      public void handle(org.vertx.java.core.http.ServerWebSocket socket) {
        if (socket.path().equals(TEST_URI)) {
          websocketHandler.handle(socket);
        }
      }
    });
    server.listen(port);
  }

  @Override
  protected void stopServer() {
    server.close();
  }

  @Test
  public void unwrap() throws Throwable {
    websocketAction(new Action<ServerWebSocket>() {
      @Override
      public void on(ServerWebSocket ws) {
        threadAssertTrue(ws.unwrap(org.vertx.java.core.http.ServerWebSocket.class) instanceof org.vertx.java.core.http.ServerWebSocket);
        resume();
      }
    });
    client.connect(new WebSocketAdapter(), URI.create(uri()));
    await();
  }

}
