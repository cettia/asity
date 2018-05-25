Github repository contains latest information about the project for now (The website will be updated later).

# Asity
#### Build web framework-agnostic applications on the JVM

Asity is a lightweight abstraction layer for web frameworks, which is designed to build applications and frameworks that can run on any full-stack framework, any micro framework or any raw server on the JVM without degrading the underlying framework's performance. It provides HTTP and WebSocket abstractions.

### HTTP

`io.cettia.asity:asity-http` provides an HTTP abstraction. Here's an echo HTTP server:

```java
Action<ServerHttpExchange> httpAction = (ServerHttpExchange http) -> {
  // Request properties
  System.out.println(http.method() + " " + http.uri());
  http.headerNames().stream().forEach(name -> System.out.println(name + ": " + http.header(name)));
  
  // Sets 200 OK response status
  http.setStatus(HttpStatus.OK);
  
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
```

### WebSocket

`io.cettia.asity:asity-websocket` provides a WebSocket abstraction. Here's an echo WebSocket server:

```java
Action<ServerWebSocket> wsAction = (ServerWebSocket ws) -> {
  // Handshake request properties
  System.out.println(HttpMethod.GET + " " + ws.uri());
  ws.headerNames().stream().forEach(name -> System.out.println(name + ": " + ws.header(name)));
  
  // When a text frame is arrived, sends it back
  ws.ontext((String data) -> ws.send(data));
  
  // When a binary frame is arrived, sends it back
  ws.onbinary((ByteBuffer bytes) -> ws.send(bytes));
  
  // When some error happens in the connection,
  ws.onerror((Throwable t) -> t.printStackTrace());
  
  // When the connection is closed for any reason,
  ws.onclose((Void v) -> System.out.println("on close"));
};
```

### Bridge

`io.cettia.asity:asity-bridge-xxx` is a module to process and convert framework-specific resources into Asity's `ServerHttpExchange` and `ServerWebSocket`. The following bridges are available, which means that an application or framework based on Asity can run on the following frameworks seamlessly:

* Atmosphere 2
* Grizzly 2
* Java Servlet 3
* Java WebSocket API 1
* Netty 4
* Spring WebFlux 5
* Vert.x 2 and 3

For details of how to set up a bridge module, check tests of each bridge module.

Please let us, [Cettia Groups](http://groups.google.com/group/cettia), know if you have any question or feedback.
