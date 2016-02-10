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
package io.cettia.asity.bridge.grizzly2;

import io.cettia.asity.action.Action;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.test.ServerHttpExchangeTestBase;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.junit.Test;

/**
 * @author Donghwan Kim
 */
public class GrizzlyServerHttpExchangeTest extends ServerHttpExchangeTestBase {

  private HttpServer server;

  @Override
  protected void startServer(int port, Action<ServerHttpExchange> requestAction) throws Exception {
    server = HttpServer.createSimpleServer(null, port);
    ServerConfiguration config = server.getServerConfiguration();
    config.addHttpHandler(new AsityHttpHandler().onhttp(requestAction), TEST_URI);
    server.start();
  }

  @Override
  protected void stopServer() throws Exception {
    server.shutdownNow();
  }

  @Test
  public void unwrap() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        threadAssertTrue(http.unwrap(Request.class) instanceof Request);
        threadAssertTrue(http.unwrap(Response.class) instanceof Response);
        resume();
      }
    });
    client.newRequest(uri()).send(new org.eclipse.jetty.client.api.Response.Listener.Adapter());
    await();
  }

}
