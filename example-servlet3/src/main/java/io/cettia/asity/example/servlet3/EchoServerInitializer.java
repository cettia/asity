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
package io.cettia.asity.example.servlet3;

import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.servlet3.AsityServlet;
import io.cettia.asity.example.echo.HttpEchoServer;
import io.cettia.asity.http.ServerHttpExchange;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

@WebListener
public class EchoServerInitializer implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    // Web fragments
    Action<ServerHttpExchange> httpAction = new HttpEchoServer();

    ServletContext context = event.getServletContext();
    Servlet servlet = new AsityServlet().onhttp(httpAction);
    ServletRegistration.Dynamic reg = context.addServlet(AsityServlet.class.getName(), servlet);
    reg.setAsyncSupported(true);
    reg.addMapping("/echo");
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
}