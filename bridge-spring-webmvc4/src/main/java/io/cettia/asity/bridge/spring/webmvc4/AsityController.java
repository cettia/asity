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
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.bridge.servlet3.ServletServerHttpExchange;
import io.cettia.asity.http.ServerHttpExchange;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller to provide {@link ServletServerHttpExchange}.
 * <p/>
 * <pre>
 *{@literal @}Bean
 * public AsityController asityController() {
 *   return new AsityController().onhttp(http -&gt; {});
 * }
 *
 *{@literal @}Bean
 * public HandlerMapping httpMapping() {
 *   AbstractHandlerMapping mapping = new AbstractHandlerMapping() {
 *  {@literal @}Override
 *   protected Object getHandlerInternal(HttpServletRequest request) {
 *     //Check whether a path equals '/test'
 *     return "/test".equals(request.getRequestURI()) &&
 *       // Delegates WebSocket handshake requests to a webSocketHandler bean
 *       !"websocket".equalsIgnoreCase(request.getHeader("upgrade")) ? asityController() : null;
 *     }
 *   };
 *   mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
 *   return mapping;
 * }
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityController implements Controller {

  private Actions<ServerHttpExchange> httpActions = new ConcurrentActions<>();

  @Override
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
    httpActions.fire(new ServletServerHttpExchange(request, response));
    return null;
  }

  /**
   * Registers an action to be called when {@link ServerHttpExchange} is available.
   */
  public AsityController onhttp(Action<ServerHttpExchange> action) {
    httpActions.add(action);
    return this;
  }

}
