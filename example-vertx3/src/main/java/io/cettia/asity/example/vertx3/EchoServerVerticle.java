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
package io.cettia.asity.example.vertx3;

import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.vertx3.AsityRequestHandler;
import io.cettia.asity.bridge.vertx3.AsityWebSocketHandler;
import io.cettia.asity.example.echo.HttpEchoServer;
import io.cettia.asity.example.echo.WebSocketEchoServer;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.websocket.ServerWebSocket;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;

public class EchoServerVerticle extends AbstractVerticle {
  @Override
  public void start() {
    // Web fragments
    Action<ServerHttpExchange> httpAction = new HttpEchoServer();
    Action<ServerWebSocket> wsAction = new WebSocketEchoServer();

    HttpServer httpServer = vertx.createHttpServer();
    AsityRequestHandler asityRequestHandler = new AsityRequestHandler().onhttp(httpAction);
    httpServer.requestHandler(request -> {
      if (request.path().equals("/echo")) {
        asityRequestHandler.handle(request);
      }
    });
    AsityWebSocketHandler asityWebsocketHandler = new AsityWebSocketHandler().onwebsocket(wsAction);
    httpServer.websocketHandler(socket -> {
      if (socket.path().equals("/echo")) {
        asityWebsocketHandler.handle(socket);
      }
    });
    httpServer.listen(8080);
  }
}
