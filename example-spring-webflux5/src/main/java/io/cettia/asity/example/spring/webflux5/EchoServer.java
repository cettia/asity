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
package io.cettia.asity.example.spring.webflux5;

import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.spring.webflux5.AsityHandlerFunction;
import io.cettia.asity.bridge.spring.webflux5.AsityWebSocketHandler;
import io.cettia.asity.example.echo.HttpEchoServer;
import io.cettia.asity.example.echo.WebSocketEchoServer;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.websocket.ServerWebSocket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.headers;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

@SpringBootApplication
@EnableWebFlux
public class EchoServer {
  @Bean
  public Action<ServerHttpExchange> httpAction() {
    return new HttpEchoServer();
  }

  @Bean
  public Action<ServerWebSocket> wsAction() {
    return new WebSocketEchoServer();
  }

  @Bean
  public RouterFunction<ServerResponse> httpMapping() {
    AsityHandlerFunction asityHandlerFunction = new AsityHandlerFunction().onhttp(httpAction());

    return RouterFunctions.route(
      path("/echo")
        // Excludes WebSocket handshake requests
        .and(headers(headers -> !"websocket".equalsIgnoreCase(headers.asHttpHeaders().getUpgrade()))), asityHandlerFunction);
  }

  @Bean
  public HandlerMapping wsMapping() {
    AsityWebSocketHandler asityWebSocketHandler = new AsityWebSocketHandler().onwebsocket(wsAction());
    Map<String, WebSocketHandler> map = new LinkedHashMap<>();
    map.put("/echo", asityWebSocketHandler);

    SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
    mapping.setUrlMap(map);

    return mapping;
  }

  @Bean
  public WebSocketHandlerAdapter webSocketHandlerAdapter() {
    return new WebSocketHandlerAdapter();
  }

  public static void main(String[] args) {
    SpringApplication.run(EchoServer.class, args);
  }
}
