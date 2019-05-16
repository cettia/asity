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

import akka.actor.PoisonPill;
import akka.util.ByteString;
import io.cettia.asity.websocket.AbstractServerWebSocket;
import io.cettia.asity.websocket.ServerWebSocket;
import play.api.http.websocket.CloseCodes;
import play.http.websocket.Message;
import play.mvc.Http;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * {@link ServerWebSocket} for Play framework 2.
 *
 * @author Donghwan Kim
 */
public class PlayServerWebSocket extends AbstractServerWebSocket {

  private AsityWebSocketActor actor;
  private Http.RequestHeader request;

  public PlayServerWebSocket(Http.RequestHeader request) {
    this.request = request;
  }

  void setActor(AsityWebSocketActor actor) {
    this.actor = actor;
  }

  @Override
  public String uri() {
    return request.uri();
  }

  @Override
  public Set<String> headerNames() {
    return request.getHeaders().toMap().keySet();
  }

  @Override
  public List<String> headers(String name) {
    return request.getHeaders().getAll(name);
  }

  void onMessage(Message.Text message) {
    textActions.fire(message.data());
  }

  void onMessage(Message.Binary message) {
    binaryActions.fire(message.data().toByteBuffer());
  }

  void onClose() {
    closeActions.fire();
  }

  @Override
  protected void doSend(ByteBuffer data) {
    actor.out.tell(new Message.Binary(ByteString.fromByteBuffer(data)), actor.self());
  }

  @Override
  protected void doSend(String data) {
    actor.out.tell(new Message.Text(data), actor.self());
  }

  @Override
  protected void doClose() {
    actor.out.tell(new Message.Close(CloseCodes.Regular()), actor.self());
    actor.self().tell(PoisonPill.getInstance(), actor.self());
  }

  /**
   * {@link AsityWebSocketActor} and {@link Http.RequestHeader} are available.
   */
  @Override
  public <T> T unwrap(Class<T> clazz) {
    return AsityWebSocketActor.class.isAssignableFrom(clazz) ?
      clazz.cast(actor) :
      Http.RequestHeader.class.isAssignableFrom(clazz) ?
        clazz.cast(request) :
        null;
  }

}
