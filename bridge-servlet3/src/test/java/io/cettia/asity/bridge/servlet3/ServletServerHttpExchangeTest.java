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
package io.cettia.asity.bridge.servlet3;

import io.cettia.asity.action.Action;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.test.ServerHttpExchangeTestBase;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Donghwan Kim
 */
public class ServletServerHttpExchangeTest extends ServerHttpExchangeTestBase {

  private Server server;

  @Override
  protected void startServer(int port, final Action<ServerHttpExchange> requestAction) throws Exception {
    server = new Server();
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.addConnector(connector);
    ServletContextHandler handler = new ServletContextHandler();
    handler.addEventListener(new ServletContextListener() {
      @Override
      public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();
        Servlet servlet = new AsityServlet().onhttp(requestAction);
        ServletRegistration.Dynamic reg = context.addServlet(AsityServlet.class.getName(), servlet);
        reg.setAsyncSupported(true);
        reg.addMapping(TEST_URI);
      }

      @Override
      public void contextDestroyed(ServletContextEvent sce) {
      }
    });
    server.setHandler(handler);
    server.start();
  }

  @Override
  protected void stopServer() throws Exception {
    server.stop();
  }

  @Test
  public void unwrap() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        threadAssertTrue(http.unwrap(HttpServletRequest.class) instanceof HttpServletRequest);
        threadAssertTrue(http.unwrap(HttpServletResponse.class) instanceof HttpServletResponse);
        resume();
      }
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
