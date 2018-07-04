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
package io.cettia.asity.example.vertx2;

import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.vertx2.AsityRequestHandler;
import io.cettia.asity.bridge.vertx2.AsityWebSocketHandler;
import io.cettia.asity.example.echo.HttpEchoServer;
import io.cettia.asity.example.echo.WebSocketEchoServer;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.websocket.ServerWebSocket;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

public class EchoServerVerticle extends Verticle {
  @Override
  public void start() {
    // Web fragments
    Action<ServerHttpExchange> httpAction = new HttpEchoServer();
    Action<ServerWebSocket> wsAction = new WebSocketEchoServer();

    HttpServer httpServer = vertx.createHttpServer();
    RouteMatcher httpMatcher = new RouteMatcher();
    httpMatcher.all("/echo", new AsityRequestHandler().onhttp(httpAction));
    httpServer.requestHandler(httpMatcher);
    AsityWebSocketHandler websocketHandler = new AsityWebSocketHandler().onwebsocket(wsAction);
    httpServer.websocketHandler(socket -> {
      if (socket.path().equals("/echo")) {
        websocketHandler.handle(socket);
      }
    });
    httpServer.listen(8080);
  }
}
