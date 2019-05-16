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
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.http.ServerHttpExchange;
import play.mvc.Http;
import play.mvc.Result;
import play.routing.RequestFunctions;

import java.util.concurrent.CompletionStage;

/**
 * An action to provide {@link PlayServerHttpExchange}.
 * <p/>
 * <pre>
 * public CompletionStage&lt;Result&gt; http(Http.Request request) {
 *   AsityHttpAction asityHttpAction = new AsityHttpAction();
 *   asityHttpAction.onhttp(http -&gt; {});
 *
 *   return asityHttpAction.apply(request);
 * }
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityHttpAction implements RequestFunctions.Params0<CompletionStage<Result>> {

  private Actions<ServerHttpExchange> httpActions = new ConcurrentActions<>();

  @Override
  public CompletionStage<Result> apply(Http.Request request) {
    PlayServerHttpExchange http = new PlayServerHttpExchange(request);
    httpActions.fire(http);
    return http.getResultFuture();
  }

  /**
   * Registers an action to be called when {@link ServerHttpExchange} is available.
   */
  public AsityHttpAction onhttp(Action<ServerHttpExchange> action) {
    httpActions.add(action);
    return this;
  }

}
