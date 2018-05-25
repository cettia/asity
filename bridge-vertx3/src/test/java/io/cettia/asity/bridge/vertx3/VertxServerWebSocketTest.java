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
import io.cettia.asity.test.ServerWebSocketTestBase;
import io.cettia.asity.websocket.ServerWebSocket;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.junit.Test;

import java.net.URI;

/**
 * @author Donghwan Kim
 */
public class VertxServerWebSocketTest extends ServerWebSocketTestBase {

  private HttpServer server;

  @Override
  protected void startServer(int port, Action<ServerWebSocket> websocketAction) {
    server = Vertx.vertx().createHttpServer();
    AsityWebSocketHandler websocketHandler = new AsityWebSocketHandler().onwebsocket(websocketAction);
    server.websocketHandler(socket -> {
      if (socket.path().equals(TEST_URI)) {
        websocketHandler.handle(socket);
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
    websocketAction(ws -> {
      threadAssertTrue(ws.unwrap(io.vertx.core.http.ServerWebSocket.class) instanceof io
        .vertx.core.http.ServerWebSocket);
      resume();
    });
    client.connect(new WebSocketAdapter(), URI.create(uri()));
    await();
  }

}
