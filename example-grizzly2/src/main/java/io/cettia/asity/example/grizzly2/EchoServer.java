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
package io.cettia.asity.example.grizzly2;

import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.grizzly2.AsityHttpHandler;
import io.cettia.asity.bridge.grizzly2.AsityWebSocketApplication;
import io.cettia.asity.example.echo.HttpEchoServer;
import io.cettia.asity.example.echo.WebSocketEchoServer;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.websocket.ServerWebSocket;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;

public class EchoServer {
  public static void main(String[] args) throws Exception {
    // Web fragments
    Action<ServerHttpExchange> httpAction = new HttpEchoServer();
    Action<ServerWebSocket> wsAction = new WebSocketEchoServer();

    HttpServer httpServer = HttpServer.createSimpleServer();
    ServerConfiguration config = httpServer.getServerConfiguration();
    config.addHttpHandler(new AsityHttpHandler().onhttp(httpAction), "/echo");
    NetworkListener listener = httpServer.getListener("grizzly");
    listener.registerAddOn(new WebSocketAddOn());
    WebSocketEngine.getEngine().register("", "/echo", new AsityWebSocketApplication().onwebsocket(wsAction));
    httpServer.start();

    System.in.read();
  }
}