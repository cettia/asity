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
package io.cettia.asity.bridge.spring.webflux5;

import io.cettia.asity.test.ServerHttpExchangeTestBase;
import io.cettia.asity.test.ServerWebSocketTestBase;
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

/**
 * @author Donghwan Kim
 */
@SpringBootApplication
@EnableWebFlux
public class TestApplication {

  @Bean
  public AsityHandlerFunction handlerFunction() {
    return new AsityHandlerFunction();
  }

  @Bean
  public RouterFunction<ServerResponse> httpMapping() {
    return RouterFunctions.route(
      path(ServerHttpExchangeTestBase.TEST_PATH)
        .and(headers(headers -> !"websocket".equalsIgnoreCase(headers.asHttpHeaders().getUpgrade()))), handlerFunction()
    );
  }

  @Bean
  public AsityWebSocketHandler webSocketHandler() {
    return new AsityWebSocketHandler();
  }

  @Bean
  public HandlerMapping wsMapping() {
    Map<String, WebSocketHandler> map = new LinkedHashMap<>();
    map.put(ServerWebSocketTestBase.TEST_PATH, webSocketHandler());

    SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
    mapping.setUrlMap(map);

    return mapping;
  }

  @Bean
  public WebSocketHandlerAdapter webSocketHandlerAdapter() {
    return new WebSocketHandlerAdapter();
  }

}
