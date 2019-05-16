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
package io.cettia.asity.bridge.play2;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import io.cettia.asity.action.Action;
import io.cettia.asity.websocket.ServerWebSocket;
import play.mvc.Controller;
import play.mvc.WebSocket;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Donghwan Kim
 */
@Singleton
public class WebSocketController extends Controller {

  private final ActorSystem actorSystem;
  private final Materializer materializer;
  private Action<ServerWebSocket> action;

  @Inject
  public WebSocketController(ActorSystem actorSystem, Materializer materializer) {
    this.actorSystem = actorSystem;
    this.materializer = materializer;
  }

  public WebSocket websocket() {
    AsityWebSocket webSocket = new AsityWebSocket(actorSystem, materializer);
    webSocket.onwebsocket(action);

    return webSocket;
  }

  public void setAction(Action<ServerWebSocket> action) {
    this.action = action;
  }

}
