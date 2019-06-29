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
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.test.ServerHttpExchangeTestBase;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpFields;
import org.junit.Ignore;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.test.Helpers;
import play.test.TestServer;

/**
 * @author Donghwan Kim
 */
public class PlayServerHttpExchangeTest extends ServerHttpExchangeTestBase {

  private TestServer server;

  @Override
  protected void startServer(int port, Action<ServerHttpExchange> action) {
    Application app = new GuiceApplicationBuilder().build();
    HttpController controller = app.injector().instanceOf(HttpController.class);
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
    requestAction(http -> {
      threadAssertTrue(http.unwrap(Http.Request.class) instanceof Http.Request);
      resume();
    });
    client.newRequest(uri()).send(new Response.Listener.Adapter());
    await();
  }

  @Test
  public void setContentTypeHeader() throws Throwable {
    requestAction(http -> {
      http.setHeader("content-type", "application/json").end();
    });
    client.newRequest(uri()).send(new Response.Listener.Adapter() {
      @Override
      public void onSuccess(Response res) {
        HttpFields headers = res.getHeaders();
        threadAssertEquals(headers.get("content-type"), "application/json");
        resume();
      }
    });
    await();
  }


  @Override
  @Test
  @Ignore
  public void testOnclose() {
  }

  @Override
  @Test
  @Ignore
  public void testReadAsync() {
  }

}
