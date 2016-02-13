---
layout: project
title: Asity Reference
---

<h1>Reference</h1>

---

**Table of Contents**

* TOC
{:toc}

---

## Getting started
Asity requires Java 7 and is distributed through Maven Central.

* **To run an Asity application**

Generally speaking, having an Asity application run on the specific platform means to feed resources like `ServerHttpExchange` and `ServerWebSocket` produced by the specific platform into the application using the corresponding bridge module. To deal with bridge, see [Platform](#platform) and [Platform on platform](#platform-on-platform) section.

* **To write an Asity application**

An Asity application is a collection of actions that handles resources like `ServerHttpExchange` and `ServerWebSocket`. Therefore, application should expose those actions to receive resources from the specific platform. To deal with resource, see [HTTP](#http) and [Websocket](#websocket) respectively.

---

## Platform
Platform stands for lietrally platform where application runs by facilitating dealing with resource like HTTP exchange and WebSocket like full-stack application framework and raw web server.

### Atmosphere 2
[Atmosphere 2](https://github.com/Atmosphere/atmosphere/) is a platform to use Java Servlet 3 and Java WebSocket API 1 together in more comfortable way.

**Note**

* Requires Atmosphere 2.2 and later.
* Servlet can't detect disconnection so that `ServerHttpExchange`'s `onclose` doesn't work.

**[Example](https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/atmosphere2)**

Add the following dependency to your build or include it on your classpath manually.

```xml
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-bridge-atmosphere2</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
```

To bridge application and Atmosphere, you should register a servlet of `AsityAtmosphereServlet`. When registering servlet, you must set `asyncSupported` to `true` and set a init param, `org.atmosphere.cpr.AtmosphereInterceptor.disableDefaults` that is defined as `org.atmosphere.cpr.ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTOR`, to `true`.

```java
@WebListener
public class Bootstrap implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    // Your application
    Action<ServerHttpExchange> httpAction = http -> {};
    Action<ServerWebSocket> websocketAction = ws -> {};
    
    ServletContext context = event.getServletContext();
    Servlet servlet = new AsityAtmosphereServlet().onhttp(httpAction).onwebsocket(websocketAction);
    ServletRegistration.Dynamic reg = context.addServlet(AsityAtmosphereServlet.class.getName(), servlet);
    reg.setAsyncSupported(true);
    reg.setInitParameter(ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTOR, Boolean.TRUE.toString());
    reg.addMapping("/cettia");
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
}
```

### Grizzly 2
[Grizzly 2](https://grizzly.java.net/) is a framework to help developers to take advantage of the Java™ NIO API.

**[Example](https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/grizzly2)**

Add the following dependency to your build or include it on your classpath manually.

```xml
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-bridge-grizzly2</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
```

And then, you should register an instance of `AsityHttpHandler` to deal with HTTP exchange and an instance of `AsityWebSocketApplication` to deal with WebSocket.

```java
public class Bootstrap {
  public static void main(String[] args) throws Exception {
    // Your application
    Action<ServerHttpExchange> httpAction = http -> {};
    Action<ServerWebSocket> websocketAction = ws -> {};
    
    HttpServer httpServer = HttpServer.createSimpleServer();
    ServerConfiguration config = httpServer.getServerConfiguration();
    config.addHttpHandler(new AsityHttpHandler().onhttp(httpAction), "/cettia");
    NetworkListener listener = httpServer.getListener("grizzly");
    listener.registerAddOn(new WebSocketAddOn());
    WebSocketEngine.getEngine().register("", "/cettia", new AsityWebSocketApplication().onwebsocket(websocketAction));
    httpServer.start();
    System.in.read();
  }
}
```

### Java Servlet 3
[Java Servlet 3.0](http://docs.oracle.com/javaee/6/tutorial/doc/bnafd.html) from Java EE 6 and [Java Servlet 3.1](https://docs.oracle.com/javaee/7/tutorial/servlets.htm) from Java EE 7.

**Note**

* Servlet can't detect disconnection so that `ServerHttpExchange`'s `onclose` doesn't work.

**[Example](https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/servlet3)**

Add the following dependency to your build or include it on your classpath manually.

```xml
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-bridge-servlet3</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
```

To bridge application and Servlet, you should register a servlet of `AsityServlet`. When registering servlet, you must set `asyncSupported` to `true`.

```java
@WebListener
public class Bootstrap implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    // Your application
    Action<ServerHttpExchange> httpAction = http -> {};
    
    ServletContext context = event.getServletContext();
    Servlet servlet = new AsityServlet().onhttp(httpAction);
    ServletRegistration.Dynamic reg = context.addServlet(AsityServlet.class.getName(), servlet);
    reg.setAsyncSupported(true);
    reg.addMapping("/cettia");
  }
  
  @Override
  public void contextDestroyed(ServletContextEvent sce) {}
}
```

With this bridge, you have no way to handle WebSocket resource unless your web server implements Java WebSocket API 1 as well as Java Servlet 3 like Tomcat or Jetty. If you have such server, see [an example demonstrating how to use Java Servlet 3 bridge and Java WebSocket API 1 bridge together on the same server](https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/servlet3-jwa1).

### Java WebSocket API 1
[Java WebSocket API 1](http://docs.oracle.com/javaee/7/tutorial/doc/websocket.htm#GKJIQ5) (JWA) from Java EE 7.

**[Example](https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/jwa1)**

Add the following dependency to your build or include it on your classpath manually.

```xml
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-bridge-jwa1</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
```

Then, you should register an endpoint of `AsityServerEndpoint`. Note that each WebSocket session is supposed to have each endpoint instance so an instance of `AsityServerEndpoint` can't be shared among `ServerEndpointConfig`s.

```java
public class Bootstrap implements ServerApplicationConfig {
  @Override
  public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {
    // Your application
    Action<ServerWebSocket> websocketAction = ws -> {};
    
    ServerEndpointConfig config = ServerEndpointConfig.Builder.create(AsityServerEndpoint.class, "/cettia")
    .configurator(new Configurator() {
      @Override
      public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        return endpointClass.cast(new AsityServerEndpoint().onwebsocket(websocketAction));
      }
    })
    .build();
    return Collections.singleton(config);
  }

  @Override
  public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
    return null;
  }
}
```

With this bridge, you have no way to handle HTTP resource unless your web server implements Java Servlet 3 as well as Java WebSocket API 1 like Tomcat or Jetty. If you have such server, see [an example demonstrating how to use Java WebSocket API 1 bridge and Java Servlet 3 bridge together on the same server](https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/servlet3-jwa1).

### Netty 4
[Netty 4](http://netty.io/) is an asynchronous event-driven network application framework.

**[Example](https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/netty4)**

Add the following dependency to your build or include it on your classpath manually.

```xml
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-bridge-netty4</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
```

To bridge application and Netty, you should register a handler of `AsityServerCodec`. When configuring handlers, you must add `HttpServerCodec` in front of the handler.

```java
public class Bootstrap {
  public static void main(String[] args) throws Exception {
    // Your application
    Action<ServerHttpExchange> httpAction = http -> {};
    Action<ServerWebSocket> websocketAction = ws -> {};
    
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap.group(bossGroup, workerGroup)
      .channel(NioServerSocketChannel.class)
      .childHandler(new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel ch) {
          ChannelPipeline pipeline = ch.pipeline();
          pipeline.addLast(new HttpServerCodec())
          .addLast(new AsityServerCodec() {
            @Override
            protected boolean accept(HttpRequest req) {
              return URI.create(req.getUri()).getPath().equals("/cettia");
            }
          }
          .onhttp(httpAction)
          .onwebsocket(websocketAction));
        }
      });
      Channel channel = bootstrap.bind(8080).sync().channel();
      channel.closeFuture().sync();
    } finally {
      workerGroup.shutdownGracefully();
      bossGroup.shutdownGracefully();
    }
  }
}
```

### Vert.x 2
[Vert.x 2](http://vertx.io/) is a lightweight, high performance application platform for the JVM 

**[Example](https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform/vertx2)**

Add the following dependency to your build or include it on your classpath manually.

```xml
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-bridge-vertx2</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
```

You should register a handler of `AsityRequestHandler` to handle HTTP exchange and `AsityWebSocketHandler` to handle WebSocket.

```java
public class Bootstrap extends Verticle {
  @Override
  public void start() {
    // Your application
    Action<ServerHttpExchange> httpAction = http -> {};
    Action<ServerWebSocket> websocketAction = ws -> {};
    
    HttpServer httpServer = vertx.createHttpServer();
    RouteMatcher httpMatcher = new RouteMatcher();
    httpMatcher.all("/cettia", new AsityRequestHandler().onhttp(httpAction));
    httpServer.requestHandler(httpMatcher);
    final AsityWebSocketHandler websocketHandler = new AsityWebSocketHandler().onwebsocket(websocketAction);
    httpServer.websocketHandler(new Handler<org.vertx.java.core.http.ServerWebSocket>() {
      @Override
      public void handle(org.vertx.java.core.http.ServerWebSocket socket) {
        if (socket.path().equals("/cettia")) {
          websocketHandler.handle(socket);
        }
      }
    });
    httpServer.listen(8080);
  }
}
```

---

## Platform on platform
Some platform, A, is based on the other platform, B, and allows to deal with the underlying platform, B, so that if a bridge for B is available, without creating an additional bridge for A, it's possible to run application on A through B.

The general pattern is to share an application instance between the platform, A, and the underlying platform, B, using `static` keyword, application holder or dependency injection framework like Spring or Guice.

### JAX-RS 2
[JAX-RS 2](https://docs.oracle.com/javaee/7/tutorial/doc/jaxws.htm) from Java EE 7. JAX-RS allows to deploy JAX-RS resources to several servers, and one of them is Java Servlet. That means, you can run application written in JAX-RS through Servlet. The same approach may be applied to JAX-RS 1. [Example](https://github.com/cettia/cettia-examples/tree/master/archetype/cettia-java-server/platform-on-platform/jaxrs2-atmosphere2).

---

## HTTP
To write HTTP application, add the following dependency to your build or include it on your classpath manually.

```xml
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-http</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
```

### `ServerHttpExchange` 
It represents a server-side HTTP request-response exchange and is given when request headers is read. Note that it is not thread safe.

#### Request properties
These are read only and might not be available in some platforms after `onend` or `onclose`.

<div class="row">
  <div class="large-4 columns">
    <h5>URI</h5>
    <p>A request URI used to connect. To work with URI parts, use <code>java.net.URI</code> or something like that.</p>
{% capture panel %}
```java
URI.create(http.uri()).getQuery();
```
{% endcapture %}{{ panel | markdownify }}
  </div>
  <div class="large-4 columns">
    <h5>Method</h5>
    <p>A name of the request method.</p>
{% capture panel %}
```java
switch (http.method()) {
  case GET:
  case POST:
    // GET or POST
    break;
}
```
{% endcapture %}{{ panel | markdownify }}
  </div>
  <div class="large-4 columns">
    <h5>Headers</h5>
    <p>Request headers.</p>
{% capture panel %}
```java
for (String name : http.headerNames()) {
  String value = http.header(name);
}
```
{% endcapture %}{{ panel | markdownify }}
  </div>
</div>

#### Reading request
`read` initiates reading the request body and a read chunk is passed to `onchunk`. Whether to read as text or binary is determined by the content-type request header in conformance with [RFC 2616](http://www.w3.org/Protocols/rfc2616/rfc2616-sec7.html#sec7.2.1). If the header starts with `text/`, chunk will be read as text following the specified charset in the header (`ISO-8859-1` if not specified) and passed as `String`. If not, chunk will be read as binary and passed as `ByteBuffer`. But you can force the request to how to read the body using `readAsText` and `readAsBinary`. Finally, the request is fully read. Then, `onend` is fired which is the end of the request.

```java
Stringbuilder bodyBuilder = new Stringbuilder();
http.onchunk(new Action<String>() {
  @Override
  public void on(String chunk) {
    bodyBuilder.append(chunk);
  }
})
.onend(new VoidAction() {
  @Override
  public void on() {
    String body = bodyBuilder.toString();
    // Your logic here
  }
})
.read();
```

For convenience, `onbody` is provided which allows to receive the whole request body. However, note that if body is quite big it will drain memory in an instant.

```java
http.onbody(new Action<String>() {
  @Override
  public void on(String body) {
    // Your logic here
  }
})
.read();
```

`read` and its variant methods should be called after adding `chunk` and `body` event handlers. In case where the underlying platform can't read body asynchronously, it emulates non-blocking IO by spwaning a separate thread to read it.

#### Response properties
These are write only and not modifiable after the write of first chunk.

<div class="row">
  <div class="large-6 columns">
    <h5>Status</h5>
    <p>A HTTP Status code for response.</p>
{% capture panel %}
```java
http.setStatus(HttpStatus.NOT_IMPLEMENTED);
```
{% endcapture %}{{ panel | markdownify }}
  </div>
  <div class="large-6 columns">
    <h5>Headers</h5>
    <p>Response headers.</p>
{% capture panel %}
```java
http.setHeader("content-type", "text/javascript; charset=utf-8");
```
{% endcapture %}{{ panel | markdownify }}
  </div>
</div>

#### Writing response
`write` accepts a text chunk as `String` and a binary chunk as `ByteBuffer` and writes it to the response body. Each response must be completed by `end` after writing all properties and chunks or even if there is nothing to write. It's the end of the response. In case of text chunk, if there is no specified charset parameter in the content-type response header or `write`, then `ISO-8859-1` is used.

```java
http.write("chunk").end();
```

For convenience, `end` accepts chunk like `write`. The below code is the same with the above one.

```java
http.end("chunk");
```

And, on the end of the response, `onfinish` is fired. 

```java
http.onfinish(new VoidAction() {
  @Override
  public void on() {
    // Your logic here
  }
})
```

#### Error handling
Any error happening in request-response exchange is propagated to actions added via `onerror` with `Throwable` in question. Now `Throwable` thrown by the underlying platform are provided directly.

```java
http.onerror(new Action<Throwable>() {
  @Override
  public void on(Throwable error) {
    // Your logic here
  }
});
```

When the underlying connection is terminated for some reason like network or protocol error, actions added via `onclose` are executed. After this event, exchange should be not used.

```java
http.onclose(new VoidAction() {
  @Override
  public void on() {
    // Your logic here
  }
});
```

---

## WebSocket
To write WebSocket application, add the following dependency to your build or include it on your classpath manually.

```xml
<dependency>
  <groupId>io.cettia.asity</groupId>
  <artifactId>asity-websocket</artifactId>
  <version>1.0.0-Beta1</version>
</dependency>
```

### `ServerWebSocket`
It represents a server-side WebSocket connection and is given when WebSocket is opened. Note that is is not thread safe.

#### Properties
These are read only and might not be available in some platforms after `onclose`.

##### URI
A request URI used to connect. It comes from handshake request so doesn't start with `ws` or `wss` protocol. To work with URI parts, use `java.net.URI` or something like that.

```java
URI.create(ws.uri()).getQuery();
```

#### Receiving frame
Text frame is passed to `ontext` as `String` and binary frame is passed to `onbinary` as `ByteBuffer`. It's possible to receive both type of message through a single connection.

```java
ws.ontext(new Action<String>() {
  @Override
  public void on(String data) {
    // Your logic here
  }
})
.onbinary(new Action<ByteBuffer>() {
  @Override
  public void on(ByteBuffer data) {
    // Your logic here
  }
});
```

#### Sending frame
`send` accepts a text frame as `String` and a binary frame as `ByteBuffer` and sends it through the connection. It's possible to send both type of message through a single connection.

```java
ws.send("message");
```

#### Closing connection
`close` closes the connection. 

```java
ws.close();
```

When the connection has been closed for any reason, normally or abnormally, close event handlers added via `onclose` are executed. It's the end of WebSocket. After this event, WebSocket should be not used.
 
```java
ws.onclose(new VoidAction() {
  @Override
  public void on() {
    // Your logic here
  }
});
```

#### Error handling
Any error happening in this connection is propagated to actions added via `onerror` with `Throwable` in question. Now `Throwable` thrown by the underlying platform are provided directly.

```java
ws.onerror(new Action<Throwable>() {
  @Override
  public void on(Throwable error) {
    // Your logic here
  }
});
```
