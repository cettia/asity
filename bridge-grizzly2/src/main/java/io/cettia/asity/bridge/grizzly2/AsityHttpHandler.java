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
package io.cettia.asity.bridge.grizzly2;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.http.ServerHttpExchange;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

/**
 * HttpHandler to process {@link Request} and {@link Response} into
 * {@link GrizzlyServerHttpExchange}.
 * <p>
 * 
 * <pre>
 * ServerConfiguration config = httpServer.getServerConfiguration();
 * config.addHttpHandler(new AsityHttpHandler().onhttp(http -&gt {}), "/cettia");
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityHttpHandler extends HttpHandler {

    private Actions<ServerHttpExchange> httpActions = new ConcurrentActions<>();

    @Override
    public void service(Request request, Response response) throws Exception {
        httpActions.fire(new GrizzlyServerHttpExchange(request, response));
    }

    /**
     * Registers an action to be called when {@link ServerHttpExchange} is
     * available.
     */
    public AsityHttpHandler onhttp(Action<ServerHttpExchange> action) {
        httpActions.add(action);
        return this;
    }

}
