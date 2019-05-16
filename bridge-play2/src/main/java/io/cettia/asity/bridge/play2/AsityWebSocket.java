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
import akka.actor.Props;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Flow;
import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.websocket.ServerWebSocket;
import play.http.websocket.Message;
import play.libs.F;
import play.libs.streams.ActorFlow;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.WebSocket;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A {@link WebSocket} handler to provide Asity's {@link ServerWebSocket}. You should inject
 * {@link ActorSystem} and {@link Materializer} to a controller and pass them to
 * {@link AsityWebSocket#AsityWebSocket(ActorSystem actorSystem, Materializer materializer)}.
 * <p/>
 * <pre>
 * public WebSocket websocket() {
 *   AsityWebSocket webSocket = new AsityWebSocket(actorSystem, materializer);
 *   webSocket.onwebsocket(ws -&gt; {});
 *
 *   return webSocket;
 * }
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityWebSocket extends WebSocket {

  private Actions<ServerWebSocket> wsActions = new ConcurrentActions<>();
  private final ActorSystem actorSystem;
  private final Materializer materializer;

  public AsityWebSocket(ActorSystem actorSystem, Materializer materializer) {
    this.actorSystem = actorSystem;
    this.materializer = materializer;
  }

  @Override
  public CompletionStage<F.Either<Result, Flow<Message, Message, ?>>> apply(Http.RequestHeader request) {
    PlayServerWebSocket ws = new PlayServerWebSocket(request);

    return CompletableFuture.completedFuture(F.Either.Right(
      ActorFlow.actorRef(ref -> Props.create(AsityWebSocketActor.class, ref, wsActions, ws), 256,
        OverflowStrategy.fail(), actorSystem, materializer)
    ));
  }

  /**
   * Registers an action to be called when {@link ServerWebSocket} is available.
   */
  public AsityWebSocket onwebsocket(Action<ServerWebSocket> action) {
    wsActions.add(action);
    return this;
  }

}
