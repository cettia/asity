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
package io.cettia.asity.bridge.vertx3;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.http.ServerHttpExchange;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;

/**
 * <code>Handler&lt;HttpServerRequest&gt;</code> to provide {@link VertxServerHttpExchange}.
 * <p/>
 * <pre>
 * AsityRequestHandler requestHandler = new AsityRequestHandler().onhttp(http -&gt; {});
 * httpServer.requestHandler(request -> {
 *   if (request.path().equals("/cettia")) {
 *     requestHandler.handle(request);
 *   }
 * });
 * </pre>
 * Or
 * <p/>
 * <pre>
 * AsityRequestHandler requestHandler = new AsityRequestHandler().onhttp(http -&gt; {});
 * Router router = Router.router(vertx);
 * router.route("/cettia").handler(rc -&gt; requestHandler.handle(rc.request()));
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityRequestHandler implements Handler<HttpServerRequest> {

  private Actions<ServerHttpExchange> httpActions = new ConcurrentActions<>();

  @Override
  public void handle(HttpServerRequest request) {
    httpActions.fire(new VertxServerHttpExchange(request));
  }

  /**
   * Registers an action to be called when {@link ServerHttpExchange} is available.
   */
  public AsityRequestHandler onhttp(Action<ServerHttpExchange> action) {
    httpActions.add(action);
    return this;
  }

}
