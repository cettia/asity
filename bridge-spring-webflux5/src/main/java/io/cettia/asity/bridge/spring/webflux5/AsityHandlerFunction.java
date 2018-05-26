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
package io.cettia.asity.bridge.spring.webflux5;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.http.ServerHttpExchange;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * HandlerFunction to provide {@link SpringWebFluxServerHttpExchange}.
 * <p/>
 * <pre>
 *{@literal @}Bean
 * public AsityHandlerFunction handlerFunction() {
 *   return new AsityHandlerFunction().on(http -&gt; {});
 * }
 *
 *{@literal @}Bean
 * public RouterFunction&lt;ServerResponse&gt; httpMapping() {
 *   AsityHandlerFunction handlerFunction = handlerFunction();
 *   return RouterFunction&lt;ServerResponse&gt; routes = RouterFunctions.route(
 *     path("/cettia")
 *       // To exclude WebSocket handshake requests
 *       .and(headers(headers -&gt; !"websocket".equalsIgnoreCase(headers.asHttpHeaders().getUpgrade()))),
 *         handlerFunction);
 * }
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityHandlerFunction implements HandlerFunction<ServerResponse> {

  private Actions<ServerHttpExchange> httpActions = new ConcurrentActions<>();

  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    SpringWebFluxServerHttpExchange http = new SpringWebFluxServerHttpExchange(request);
    return http.getServerResponse().doFinally($ -> httpActions.fire(http));
  }

  /**
   * Registers an action to be called when {@link ServerHttpExchange} is available.
   */
  public AsityHandlerFunction onhttp(Action<ServerHttpExchange> action) {
    httpActions.add(action);
    return this;
  }

}
