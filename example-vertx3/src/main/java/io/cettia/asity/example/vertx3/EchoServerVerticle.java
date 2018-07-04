package io.cettia.asity.example.vertx3;

import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.vertx3.AsityRequestHandler;
import io.cettia.asity.bridge.vertx3.AsityWebSocketHandler;
import io.cettia.asity.example.echo.HttpEchoServer;
import io.cettia.asity.example.echo.WebSocketEchoServer;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.websocket.ServerWebSocket;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;

public class EchoServerVerticle extends AbstractVerticle {
  @Override
  public void start() {
    // Web fragments
    Action<ServerHttpExchange> httpAction = new HttpEchoServer();
    Action<ServerWebSocket> wsAction = new WebSocketEchoServer();

    HttpServer httpServer = vertx.createHttpServer();
    AsityRequestHandler asityRequestHandler = new AsityRequestHandler().onhttp(httpAction);
    httpServer.requestHandler(request -> {
      if (request.path().equals("/echo")) {
        asityRequestHandler.handle(request);
      }
    });
    AsityWebSocketHandler asityWebsocketHandler = new AsityWebSocketHandler().onwebsocket(wsAction);
    httpServer.websocketHandler(socket -> {
      if (socket.path().equals("/echo")) {
        asityWebsocketHandler.handle(socket);
      }
    });
    httpServer.listen(8080);
  }
}
