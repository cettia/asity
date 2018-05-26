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
import io.cettia.asity.test.ServerWebSocketTestBase;
import io.cettia.asity.websocket.ServerWebSocket;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Donghwan Kim
 */
public class SpringWebMvcServerWebSocketTest extends ServerWebSocketTestBase {

  private ConfigurableApplicationContext ctx;

  @Override
  protected void startServer(int port, Action<ServerWebSocket> websocketAction) {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("server.port", port);

    ctx = new SpringApplicationBuilder(TestApplication.class).properties(props).run();
    AsityWebSocketHandler webSocketHandler = ctx.getBean(AsityWebSocketHandler.class);
    webSocketHandler.onwebsocket(websocketAction);
  }

  @Override
  protected void stopServer() {
    SpringApplication.exit(ctx);
  }

  @Test
  public void unwrap() throws Throwable {
    websocketAction(ws -> {
      threadAssertTrue(ws.unwrap(WebSocketSession.class) instanceof WebSocketSession);
      resume();
    });
    client.connect(new WebSocketAdapter(), URI.create(uri()));
    await();
  }

}
