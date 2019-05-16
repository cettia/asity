How to run the server:

```shell
git clone https://github.com/cettia/asity.git
cd asity/example-play2
sbt run
```

FYI, you can run the application by `mvn play2:run` too.

How to run the WebSocket client:

```shell
cd asity/example
mvn package exec:java@websocket-echo-client
```

Play framework doesn't allow to read HTTP request body by chunk so the HTTP client example that does ping-pong with the server doesn't work with this example. Instead, if you send the whole request body at once, you can see it's sent back via response body.

```
curl -v -X POST -H 'Content-Type: text/plain' -d 'A message to be sent back' http://localhost:8080/echo
```
