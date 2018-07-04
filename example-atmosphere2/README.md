How to run the server:

```shell
git clone https://github.com/cettia/asity.git
cd asity/example-atmosphere2
mvn jetty:run
```

How to run the client:

```shell
cd asity/example
mvn package exec:java@websocket-echo-client exec:java@http-echo-client
```