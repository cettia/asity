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
package io.cettia.asity.bridge.vertx2;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.vertx2.AsityWebSocketHandler;
import io.cettia.asity.test.ServerWebSocketTest;
import io.cettia.asity.websocket.ServerWebSocket;

import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.http.HttpServer;

public class VertxServerWebSocketTest extends ServerWebSocketTest {

    HttpServer server;

    @Override
    protected void startServer() {
        server = VertxFactory.newVertx().createHttpServer();
        final AsityWebSocketHandler websocketHandler = new AsityWebSocketHandler().onwebsocket(performer.serverAction());
        server.websocketHandler(new Handler<org.vertx.java.core.http.ServerWebSocket>() {
            @Override
            public void handle(org.vertx.java.core.http.ServerWebSocket socket) {
                if (socket.path().equals("/test")) {
                    websocketHandler.handle(socket);
                }
            }
        });
        server.listen(port);
    }

    @Override
    protected void stopServer() {
        server.close();
    }

    @Test
    public void unwrap() {
        performer.onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                assertThat(ws.unwrap(org.vertx.java.core.http.ServerWebSocket.class),
                        instanceOf(org.vertx.java.core.http.ServerWebSocket.class));
                performer.start();
            }
        })
        .connect();
    }

}
