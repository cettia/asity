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
package io.cettia.asity.bridge.spring.webmvc4;

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

/**
 * @author Donghwan Kim
 */
@SpringBootApplication
@EnableWebMvc
@EnableWebSocket
public class TestApplication implements WebSocketConfigurer {

  @Bean
  public AsityController asityController() {
    return new AsityController();
  }

  @Bean
  public HandlerMapping httpMapping() {
    AbstractHandlerMapping mapping = new AbstractHandlerMapping() {
      @Override
      protected Object getHandlerInternal(HttpServletRequest request) {
        // Check whether a path equals '/test'
        return "/test".equals(request.getRequestURI()) &&
          // Delegates WebSocket handshake requests to a webSocketHandler bean
          !"websocket".equalsIgnoreCase(request.getHeader("upgrade")) ? asityController() : null;
      }
    };
    mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return mapping;
  }

  @Bean
  public AsityWebSocketHandler webSocketHandler() {
    return new AsityWebSocketHandler();
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(webSocketHandler(), "/test");
  }

}
