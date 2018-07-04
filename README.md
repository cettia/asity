# Asity
#### Build universally reusable web fragments on the JVM

Write echo web fragments that sends the received data back.

```java
Action httpAction = http -> http.readAsBinary().onchunk((ByteBuffer chunk) -> http.write(chunk)).onend((Void v) -> http.end());
```
```java
Action wsAction = ws -> ws.ontext((String text) -> ws.send(text)).onbinary((ByteBuffer binary) -> ws.send(binary));
```

And run across different frameworks in the Java ecosystem. Before getting started, be sure that you have Java 8+ and Maven 3+ installed.

- [example-atmosphere2](https://github.com/cettia/asity/tree/master/example-atmosphere2) 
- [example-grizzly2](https://github.com/cettia/asity/tree/master/example-grizzly2) 
- [example-jwa1](https://github.com/cettia/asity/tree/master/example-jwa1) 
- [example-netty4](https://github.com/cettia/asity/tree/master/example-netty4) 
- [example-servlet3](https://github.com/cettia/asity/tree/master/example-servlet3) 
- [example-spring-webflux5](https://github.com/cettia/asity/tree/master/example-spring-webflux5) 
- [example-spring-webmvc4](https://github.com/cettia/asity/tree/master/example-spring-webmvc4) 
- [example-vertx2](https://github.com/cettia/asity/tree/master/example-vertx2) 
- [example-vertx3](https://github.com/cettia/asity/tree/master/example-vertx3)

For more information about Asity, please visit the [Asity](http://asity.cettia.io) website and follow [@flowersits](https://twitter.com/flowersits) on Twitter.