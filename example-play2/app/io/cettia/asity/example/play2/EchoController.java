/*
 * Copyright 2019 the original author or authors.
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
package io.cettia.asity.example.play2;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import io.cettia.asity.action.Action;
import io.cettia.asity.bridge.play2.AsityHttpAction;
import io.cettia.asity.bridge.play2.AsityWebSocket;
import io.cettia.asity.example.echo.HttpEchoServer;
import io.cettia.asity.example.echo.WebSocketEchoServer;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.websocket.ServerWebSocket;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * @author Donghwan Kim
 */
public class EchoController extends Controller {

  private final Action<ServerHttpExchange> httpAction = new HttpEchoServer();
  private final Action<ServerWebSocket> wsAction = new WebSocketEchoServer();
  private final ActorSystem actorSystem;
  private final Materializer materializer;

  @Inject
  public EchoController(ActorSystem actorSystem, Materializer materializer) {
    this.actorSystem = actorSystem;
    this.materializer = materializer;
  }

  public CompletionStage<Result> http(Http.Request request) {
    AsityHttpAction action = new AsityHttpAction();
    action.onhttp(httpAction);

    return action.apply(request);
  }

  public WebSocket websocket() {
    AsityWebSocket webSocket = new AsityWebSocket(actorSystem, materializer);
    webSocket.onwebsocket(wsAction);

    return webSocket;
  }

}
