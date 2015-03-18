/*
 * Copyright 2015 The Cettia Project
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
package io.cettia.platform.bridge.vertx2;

import io.cettia.platform.action.Action;
import io.cettia.platform.action.Actions;
import io.cettia.platform.action.ConcurrentActions;
import io.cettia.platform.http.ServerHttpExchange;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerRequest;

/**
 * Handler to process {@link HttpServerRequest} into
 * {@link VertxServerHttpExchange}.
 * <p>
 * 
 * <pre>
 * httpServer.requestHandler(new CettiaRequestHandler().onhttp(http -&gt {}));
 * </pre>
 *
 * @author Donghwan Kim
 */
public class CettiaRequestHandler implements Handler<HttpServerRequest> {

    private Actions<ServerHttpExchange> httpActions = new ConcurrentActions<>();

    @Override
    public void handle(HttpServerRequest request) {
        httpActions.fire(new VertxServerHttpExchange(request));
    }

    /**
     * Registers an action to be called when {@link ServerHttpExchange} is
     * available.
     */
    public CettiaRequestHandler onhttp(Action<ServerHttpExchange> action) {
        httpActions.add(action);
        return this;
    }


}
