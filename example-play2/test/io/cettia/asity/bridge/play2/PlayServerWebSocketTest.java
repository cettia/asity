/*
 * Copyright 2019 the original author or authors.
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
package io.cettia.asity.bridge.play2;

import io.cettia.asity.action.Action;
import io.cettia.asity.test.ServerWebSocketTestBase;
import io.cettia.asity.websocket.ServerWebSocket;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.test.Helpers;
import play.test.TestServer;

import java.net.URI;

/**
 * @author Donghwan Kim
 */
public class PlayServerWebSocketTest extends ServerWebSocketTestBase {

  private TestServer server;

  @Override
  protected void startServer(int port, Action<ServerWebSocket> action) {
    Application app = new GuiceApplicationBuilder().build();
    WebSocketController controller = app.injector().instanceOf(WebSocketController.class);
    controller.setAction(action);

    server = Helpers.testServer(port, app);
    server.start();
  }

  @Override
  protected void stopServer() {
    server.stop();
  }

  @Test
  public void unwrap() throws Throwable {
    websocketAction(ws -> {
      threadAssertTrue(ws.unwrap(AsityWebSocketActor.class) instanceof AsityWebSocketActor);
      threadAssertTrue(ws.unwrap(Http.RequestHeader.class) instanceof Http.RequestHeader);
      resume();
    });
    client.connect(new WebSocketAdapter(), URI.create(uri()));
    await();
  }

}
