/*
 * Copyright 2018 the original author or authors.
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
package io.cettia.asity.bridge.spring.webflux5;

import io.cettia.asity.websocket.AbstractServerWebSocket;
import io.cettia.asity.websocket.ServerWebSocket;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * {@link ServerWebSocket} for Spring WebFlux 5.
 *
 * @author Donghwan Kim
 */
public class SpringWebFluxServerWebSocket extends AbstractServerWebSocket {

  private final WebSocketSession session;
  private final Mono<Void> mono;
  private FluxSink<WebSocketMessage> messageEmitter;

  public SpringWebFluxServerWebSocket(WebSocketSession session) {
    this.session = session;
    session.receive().subscribe(message -> {
      switch (message.getType()) {
        case TEXT:
          textActions.fire(message.getPayloadAsText());
          break;
        case BINARY:
          binaryActions.fire(message.getPayload().asByteBuffer());
          break;
        // Ignores PING and PONG
        default:
          break;
      }
    }, errorActions::fire, closeActions::fire);

    Flux<WebSocketMessage> flux = Flux.create(messageEmitter -> this.messageEmitter = messageEmitter);
    ConnectableFlux<WebSocketMessage> messages = flux.replay();
    messages.connect();
    this.mono = session.send(messages);
  }

  public Mono<Void> getMono() {
    return mono;
  }

  @Override
  public String uri() {
    URI uri = session.getHandshakeInfo().getUri();
    return uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
  }

  @Override
  public Set<String> headerNames() {
    return session.getHandshakeInfo().getHeaders().keySet();
  }

  @Override
  public List<String> headers(String name) {
    return session.getHandshakeInfo().getHeaders().get(name);
  }

  @Override
  protected void doSend(String data) {
    messageEmitter.next(session.textMessage(data));
  }

  @Override
  protected void doSend(ByteBuffer byteBuffer) {
    messageEmitter.next(session.binaryMessage(bufferFactory -> bufferFactory.wrap(byteBuffer)));
  }

  @Override
  protected void doClose() {
    messageEmitter.complete();
  }

  /**
   * {@link WebSocketSession} is available.
   */
  @Override
  public <T> T unwrap(Class<T> clazz) {
    return WebSocketSession.class.isAssignableFrom(clazz) ? clazz.cast(session) : null;
  }

}
