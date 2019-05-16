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

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.util.ByteString;
import io.cettia.asity.action.Actions;
import io.cettia.asity.websocket.ServerWebSocket;
import play.http.websocket.Message;

import java.nio.ByteBuffer;

/**
 * An actor to handle a WebSocket connection.
 *
 * @author Donghwan Kim
 */
public class AsityWebSocketActor extends AbstractActor {

  final ActorRef out;
  private final Actions<ServerWebSocket> actions;
  private final PlayServerWebSocket ws;

  public AsityWebSocketActor(ActorRef out, Actions<ServerWebSocket> actions, PlayServerWebSocket ws) {
    this.out = out;
    this.actions = actions;
    this.ws = ws;
  }

  @Override
  public void preStart() {
    ws.setActor(this);
    actions.fire(ws);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(Message.Text.class, ws::onMessage)
      .match(Message.Binary.class, ws::onMessage)
      .build();
  }

  @Override
  public void postStop() {
    ws.onClose();
  }

}
