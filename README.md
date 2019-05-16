# Asity

Asity is a lightweight abstraction layer to build universally reusable web fragments on the JVM, and web fragment represents a component that receives HTTP request-response or WebSocket connection like a controller in MVC but is able to be compatible with any web framework in the Java ecosystem.

As a web fragment author, you can write a web fragment once and support almost all popular web frameworks in Java, and as an end-user, you can choose any technology stack as you wish and use web fragments without being frustrated by compatibility issues.

Here's a comprehensive example for Asity 2. It demonstrates how to build an echo fragment which simply responds to the client with whatever data the client sent, and plug the fragment into Spring WebFlux, a web framework of Spring 5 reactive stack based on Reactive Streams.

Add the following dependencies:

```xml
<!-- To write a web fragment -->
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-http</artifactId>
  <version>2.0.0</version>
</dependency>
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-websocket</artifactId>
  <version>2.0.0</version>
</dependency>
<!-- To run a web fragment on Spring WebFlux 5 -->
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-bridge-spring-webflux5</artifactId>
  <version>2.0.0</version>
</dependency>
```

And the following class:

```java
package io.cettia.asity.example.spring.webflux5;

import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.spring.webflux5.AsityHandlerFunction;
import io.cettia.asity.bridge.spring.webflux5.AsityWebSocketHandler;
import io.cettia.asity.http.HttpStatus;
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

import java.nio.ByteBuffer;
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

    RequestPredicate isNotWebSocket = headers(h -> !"websocket".equalsIgnoreCase(h.asHttpHeaders().getUpgrade()));
    return RouterFunctions.route(path("/cettia").and(isNotWebSocket), asityHandlerFunction);
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

  public static class HttpEchoServer implements Action<ServerHttpExchange> {
    @Override
    public void on(ServerHttpExchange http) {
      // Reads request URI, method and headers
      System.out.println(http.method() + " " + http.uri());
      http.headerNames().stream().forEach(name -> System.out.println(name + ": " + String.join(", ", http.headers(name))));

      // Writes response status code and headers
      http.setStatus(HttpStatus.OK).setHeader("content-type", http.header("content-type"));

      // Reads a chunk from request body and writes it to response body
      http.readAsText().onchunk((String chunk) -> http.write(chunk));
      // If request body is binary,
      // http.readAsBinary().onchunk((ByteBuffer binary) -> http.write(binary));

      // Ends response if request ends
      http.onend((Void v) -> http.end());

      // Exception handling
      http.onerror((Throwable t) -> t.printStackTrace()).onclose((Void v) -> System.out.println("disconnected"));
    }
  }

  public static class WebSocketEchoServer implements Action<ServerWebSocket> {
    @Override
    public void on(ServerWebSocket ws) {
      // Reads handshake request URI and headers
      System.out.println(ws.uri());
      ws.headerNames().stream().forEach(name -> System.out.println(name + ": " + String.join(", ", ws.headers(name))));

      // Sends the received text frame and binary frame back
      ws.ontext((String text) -> ws.send(text)).onbinary((ByteBuffer binary) -> ws.send(binary));

      // Exception handling
      ws.onerror((Throwable t) -> t.printStackTrace());
    }
  }
}
```

As you would intuitively expect, `HttpEchoServer` and `WebSocketEchoServer` are a web fragment, and can be reused in other frameworks through other bridges like `asity-bridge-spring-webmvc4`. Also, note that a bridge implementation is completely transparent to end-users. End-users still have the full-control over web fragments on framework they selected. If they want to filter out requests, they can do that in the way they use the framework, and pass only proper requests to web fragments instead of learning how to filter out requests in Asity.

Now Asity supports Java API for WebSocket 1, Servlet 3, Spring WebFlux 5, Spring MVC 4, Vert.x 3, Netty 4, Play framework 2, Grizzly 2,
 Vert.x 2 and Atmosphere 2. Here's a list of working examples per supported framework. They include the corresponding
  client to enable you to test the example as well. The full documentation is available at the [Asity website](https://asity.cettia.io).

- [Atmosphere 2](https://github.com/cettia/asity/tree/master/example-atmosphere2)
- [Grizzly 2](https://github.com/cettia/asity/tree/master/example-grizzly2)
- [Java API for WebSocket 1](https://github.com/cettia/asity/tree/master/example-jwa1)
- [Netty 4](https://github.com/cettia/asity/tree/master/example-netty4)
- [Play framework 2](https://github.com/cettia/asity/tree/master/example-play2)
- [Servlet 3](https://github.com/cettia/asity/tree/master/example-servlet3)
- [Spring WebFlux 5](https://github.com/cettia/asity/tree/master/example-spring-webflux5)
- [Spring MVC 4](https://github.com/cettia/asity/tree/master/example-spring-webmvc4)
- [Vert.x 2](https://github.com/cettia/asity/tree/master/example-vertx2)
- [Vert.x 3](https://github.com/cettia/asity/tree/master/example-vertx3)

We sincerely believe that Asity project can make Java web development enjoyable. If you are interested in Asity's vision and would like to be more involved, feel free to join the [mailing list](http://groups.google.com/group/cettia) and share your feedback, or just DM me on Twitter ;) ([@flowersits](https://twitter.com/flowersits)).
