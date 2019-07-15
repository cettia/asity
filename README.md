# Asity

Asity is an HTTP/WebSocket abstraction layer for various web frameworks on the JVM. Asity provides a tool-kit for writing a web fragment, a function that handles HTTP request-response exchange and WebSocket, and allows to create web applications by combining web fragments.

For example, with Asity you can write a web fragment that sends back incoming WebSocket messages as follows

```java
Action<ServerWebSocket> action = ws -> ws.ontext(ws::send).onbinary(ws::send);
```

And plug it into Java API for WebSocket, Spring WebFlux, Spring MVC, Vert.x, Netty, Play Framework, Grizzly, and so on. Visit the Asity website for the full documentation.

## Supported Frameworks

Asity supports the following frameworks. Each link points to a demo project which shows how to plug the example echo web fragment to each framework.

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

## Links

Here are some useful links to learn more about Asity. If you are interested and would like to be more involved, feel free to join the mailing list and share your feedback.

- [Website](https://asity.cettia.io/)
- [Showcase](https://asity.cettia.io/#showcase)
- [Mailing list](http://groups.google.com/group/cettia)
- [Twitter](https://twitter.com/flowersits)

Asity is an open source project licensed under Apache License 2.0 and driven by the community, for the community.
