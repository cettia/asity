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
package io.cettia.asity.bridge.atmosphere2;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.websocket.ServerWebSocket;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.handler.AtmosphereHandlerAdapter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Servlet to process {@link AtmosphereResource} into {@link ServerHttpExchange}
 * and {@link ServerWebSocket}. When you configure servlet, you must set
 * <strong><code>asyncSupported</code></strong> to <strong><code>true</code>
 * </strong> and set a init param, <strong>
 * <code>org.atmosphere.cpr.AtmosphereInterceptor.disableDefaults</code>
 * </strong>, to <strong><code>true</code></strong>.
 * <p/>
 * <p/>
 * <pre>
 * Servlet servlet = new AsityAtmosphereServlet().onhttp(http -&gt; {}).onwebsocket(ws -&gt; {});
 * ServletRegistration.Dynamic reg = context.addServlet(AsityAtmosphereServlet.class.getName(),
 * servlet);
 * <strong>reg.setAsyncSupported(true);</strong>
 * <strong>reg.setInitParameter(ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTOR, Boolean.TRUE
 * .toString())</strong>
 * reg.addMapping("/cettia");
 * </pre>
 *
 * @author Donghwan Kim
 */
@SuppressWarnings("serial")
public class AsityAtmosphereServlet extends AtmosphereServlet {

  private Actions<ServerHttpExchange> httpActions = new ConcurrentActions<>();
  private Actions<ServerWebSocket> wsActions = new ConcurrentActions<>();

  @Override
  public void init(ServletConfig sc) throws ServletException {
    super.init(sc);
    framework().addAtmosphereHandler("/", new AtmosphereHandlerAdapter() {
      @Override
      public void onRequest(AtmosphereResource resource) throws IOException {
        if (isWebSocketResource(resource)) {
          if (resource.getRequest().getMethod().equals("GET")) {
            wsActions.fire(new AtmosphereServerWebSocket(resource));
          }
        } else {
          httpActions.fire(new AtmosphereServerHttpExchange(resource));
        }
      }
    });
  }

  /**
   * Does the given {@link AtmosphereResource} represent WebSocket resource?
   */
  protected boolean isWebSocketResource(AtmosphereResource resource) {
    // As HttpServletResponseWrapper, AtmosphereResponse returns itself on
    // its getResponse method when there was no instance of ServletResponse
    // given by the container. That's exactly the case of WebSocket.
    return resource.getResponse().getResponse() instanceof AtmosphereResponse;
  }

  /**
   * Registers an action to be called when {@link ServerHttpExchange} is
   * available.
   */
  public AsityAtmosphereServlet onhttp(Action<ServerHttpExchange> action) {
    httpActions.add(action);
    return this;
  }

  /**
   * Registers an action to be called when {@link ServerWebSocket} is
   * available.
   */
  public AsityAtmosphereServlet onwebsocket(Action<ServerWebSocket> action) {
    wsActions.add(action);
    return this;
  }

}
