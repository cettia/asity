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
package io.cettia.asity.bridge.vertx2;

import io.cettia.asity.action.Action;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.test.ServerHttpExchangeTestBase;

import org.eclipse.jetty.client.api.Response;
import org.junit.Test;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;

/**
 * @author Donghwan Kim
 */
public class VertxServerHttpExchangeTest extends ServerHttpExchangeTestBase {

    private HttpServer server;

    @Override
    protected void startServer(int port, Action<ServerHttpExchange> requestAction) throws Exception {
        server = VertxFactory.newVertx().createHttpServer();
        RouteMatcher matcher = new RouteMatcher();
        matcher.all(TEST_URI, new AsityRequestHandler().onhttp(requestAction));
        server.requestHandler(matcher);
        server.listen(port);
    }

    @Override
    protected void stopServer() {
        server.close();
    }

    @Test
    public void unwrap() throws Throwable {
        requestAction(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                threadAssertTrue(http.unwrap(HttpServerRequest.class) instanceof HttpServerRequest);
                resume();
            }
        });
        client.newRequest(uri()).send(new Response.Listener.Adapter());
        await();
    }

}
