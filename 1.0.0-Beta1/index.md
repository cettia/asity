---
layout: project
title: Asity
---

<h1>Asity</h1>
<h4 class="subheader">Build I/O framework-agnostic applications on the JVM</h4>

Asity is a lightweight abstraction layer for I/O frameworks which is designed to build applications that can run on any full-stack framework, any micro framework or any raw server on the JVM.

An Asity application can handle the following resources: 

<ul class="menu simple" style="margin-bottom: 1rem">
<li>HTTP</li>
<li>WebSocket</li>
</ul>

And run on the following platforms:

<ul class="menu simple">
<li>Atmosphere 2</li>
<li>Grizzly 2</li>
<li>Java Servlet 3</li>
<li>Java WebSocket API 1</li>
<li>Netty 4</li>
<li>Vert.x 2</li>
</ul>

---

## Getting started

### Write your first Asity app

Asity is distributed through Maven Central. To write a web application running on any platform Asity supports, you need two artifacts: `io.cettia.asity:asity-http:1.0.0-Beta1` and `io.cettia.asity:asity-websocket:1.0.0-Beta1`.

```xml
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-http</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-websocket</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
```

Once you've set up the build, all you need to do is to write actions receiving `ServerHttpExchange` and `ServerWebSocket`. As a simple example, let's write echo actions sending any incoming messages such as HTTP chunk and WebSocket data frame back.

```java
import io.cettia.asity.action.Action;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.websocket.ServerWebSocket;

public class EchoHandler {
  // Asity supports Java 7+
  // For better readability, public final fields and lambda expressions in Java 8 are used here
  public final Action<ServerHttpExchange> httpAction = (ServerHttpExchange http) -> {
    // Copies the content-type header of the request to the response
    http.setHeader("content-type", http.header("content-type"));
    // When a chunk is read from the request body, writes it to the response body
    http.onchunk((ByteBuffer bytes) -> http.write(bytes));
    // When the request is fully read, ends the response
    http.onend((Void v) -> http.end());
    // Reads the request body as binary to circumvent encoding issue
    http.readAsBinary();
    // When the response is fully written and ends,
    http.onfinish((Void v) -> System.out.println("on finish"));
    // When some error happens in the request-response exchange,
    http.onerror((Throwable t) -> t.printStackTrace());
    // When the underlying connection is terminated,
    http.onclose((Void v) -> System.out.println("on close"));
  };
  public final Action<ServerWebSocket> websocketAction = (ServerWebSocket ws) -> {
    // When a text frame is arrived, sends it back
    ws.ontext((String data) -> ws.send(data));
    // When a binary frame is arrived, sends it back
    ws.onbinary((ByteBuffer bytes) -> ws.send(bytes));
    // When some error happens in the connection,
    ws.onerror((Throwable t) -> t.printStackTrace());
    // When the connection is closed for any reason,
    ws.onclose((Void v) -> System.out.println("on close"));
  };
}
```

### Run your app

Now to run this application on the specific platform, we need to wrap HTTP resources and WebSocket resources provided by that specific platform into `ServerHttpExchange` and `ServerWebSocket` and feed them into an instance of `EchoHandler`. A module playing such roles is called bridge.

For example, to run `EchoHandler` on an implementation of Java Servlet 3 and Java WebSocket API 1 such as Jetty 9 and Tomcat 8, you need Java Servlet 3 bridge and Java WebSocket API 1 bridge. Let's add the following bridge dependencies.

```xml
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-bridge-servlet3</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-bridge-jwa1</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
```

Then, you can feed the application with HTTP resources through `AsityServlet` and WebSocket resources through `AsityServerEndpoint`.

```java
import io.cettia.asity.bridge.jwa1.AsityServerEndpoint;
import io.cettia.asity.bridge.servlet3.AsityServlet;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import javax.websocket.DeploymentException;
import javax.websocket.server.*;

@WebListener
public class Bootstrap implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    // An Asity application
    final EchoHandler handler = new EchoHandler();
    
    // Feeds the application with HTTP resources produced by Servlet 3
    ServletContext context = event.getServletContext();
    Servlet servlet = new AsityServlet().onhttp(handler.httpAction);
    ServletRegistration.Dynamic reg = context.addServlet(AsityServlet.class.getName(), servlet);
    reg.setAsyncSupported(true);
    reg.addMapping("/echo");
    
    // Feeds the application with WebSocket resources produced by Java WebSocket API 1
    ServerContainer container = (ServerContainer) context.getAttribute(ServerContainer.class.getName());
    ServerEndpointConfig config = ServerEndpointConfig.Builder.create(AsityServerEndpoint.class, "/echo")
    .configurator(new Configurator() {
      @Override
      public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return endpointClass.cast(new AsityServerEndpoint().onwebsocket(handler.websocketAction));
      }
    })
    .build();
    try {
      container.addEndpoint(config);
    } catch (DeploymentException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
}
```

### Try out examples

The same pattern applies when bridging an application to other platforms. Here is working examples. They demonstrate how to run [Cettia Java Server](/projects/cettia-java-server) implementing the Cettia Protocol using Asity on each platform.

<ul class="menu">
<li><a href="https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/atmosphere2">Atmosphere 2</a></li>
<li><a href="https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/grizzly2">Grizzly 2</a></li>
<li><a href="https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/jwa1">Java WebSocket API 1</a></li>
<li><a href="https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/netty4">Netty 4</a></li>
<li><a href="https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/servlet3">Servlet 3</a></li>
<li><a href="https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/servlet3-jwa1">Java Servlet 3 and Java WebSocket API 1</a></li>
<li><a href="https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/vertx2">Vert.x 2</a></li>
</ul>

It's not the end. Some platform, A, is based on the other platform, B, and allows to deal with the underlying platform, B, so that if a bridge for B is available, without creating an additional bridge for A, it's possible to run application on A through B. For example, applications written in Spring MVC platform or JAX-RS platform can run on Servlet platform. See the following examples.

<ul class="menu">
<li><a href="https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform-on-platform/jaxrs2-atmosphere2">JAX-RS 2 on Atmosphere 2</a></li>
</ul>

### Build a custom bridge

Though your favorite platform is not supported? Take a look how [Grizzly 2 bridge](https://github.com/cettia/asity/tree/1.0.0-Beta1/bridge-grizzly2) is written. Mostly, with more or less 200 lines, it's enough to write a bridge.
