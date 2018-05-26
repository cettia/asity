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
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.test.ServerHttpExchangeTestBase;
import org.eclipse.jetty.client.api.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Donghwan Kim
 */
public class SpringWebMvcServerHttpExchangeTest extends ServerHttpExchangeTestBase {

  private ConfigurableApplicationContext ctx;

  @Override
  protected void startServer(int port, Action<ServerHttpExchange> requestAction) {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("server.port", port);

    ctx = new SpringApplicationBuilder(TestApplication.class).properties(props).run();
    AsityController controller = ctx.getBean(AsityController.class);
    controller.onhttp(requestAction);
  }

  @Override
  protected void stopServer() {
    SpringApplication.exit(ctx);
  }

  @Test
  public void unwrap() throws Throwable {
    requestAction(http -> {
      threadAssertTrue(http.unwrap(HttpServletRequest.class) instanceof HttpServletRequest);
      threadAssertTrue(http.unwrap(HttpServletResponse.class) instanceof HttpServletResponse);
      resume();
    });
    client.newRequest(uri()).send(new Response.Listener.Adapter());
    await();
  }

  @Override
  @Test
  @Ignore
  public void testOnclose() {
  }

}
