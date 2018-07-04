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
package io.cettia.asity.example.spring.webmvc4;

import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.spring.webmvc4.AsityController;
import io.cettia.asity.bridge.spring.webmvc4.AsityWebSocketHandler;
import io.cettia.asity.example.echo.HttpEchoServer;
import io.cettia.asity.example.echo.WebSocketEchoServer;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.websocket.ServerWebSocket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import javax.servlet.http.HttpServletRequest;

@SpringBootApplication
@EnableWebMvc
@EnableWebSocket
public class EchoServer implements WebSocketConfigurer {
  @Bean
  public Action<ServerHttpExchange> httpAction() {
    return new HttpEchoServer();
  }

  @Bean
  public Action<ServerWebSocket> wsAction() {
    return new WebSocketEchoServer();
  }

  @Bean
  public HandlerMapping httpMapping() {
    AsityController asityController = new AsityController().onhttp(httpAction());
    AbstractHandlerMapping mapping = new AbstractHandlerMapping() {
      @Override
      protected Object getHandlerInternal(HttpServletRequest request) {
        // Check whether a path equals '/echo'
        return "/echo".equals(request.getRequestURI()) &&
          // Delegates WebSocket handshake requests to a webSocketHandler bean
          !"websocket".equalsIgnoreCase(request.getHeader("upgrade")) ? asityController : null;
      }
    };
    mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return mapping;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    AsityWebSocketHandler asityWebSocketHandler = new AsityWebSocketHandler().onwebsocket(wsAction());
    registry.addHandler(asityWebSocketHandler, "/echo");
  }

  public static void main(String[] args) {
    SpringApplication.run(EchoServer.class);
  }
}