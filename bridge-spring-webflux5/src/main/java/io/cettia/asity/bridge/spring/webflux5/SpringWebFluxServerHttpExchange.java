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

import io.cettia.asity.action.Action;
import io.cettia.asity.http.AbstractServerHttpExchange;
import io.cettia.asity.http.HttpMethod;
import io.cettia.asity.http.HttpStatus;
import io.cettia.asity.http.ServerHttpExchange;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * {@link ServerHttpExchange} for Spring WebFlux 5.
 *
 * @author Donghwan Kim
 */
public class SpringWebFluxServerHttpExchange extends AbstractServerHttpExchange {

  private final ServerRequest request;
  private final Mono<ServerResponse> serverResponse;
  private ServerHttpResponse response;
  private FluxSink<ByteBuffer> chunkEmitter;

  public SpringWebFluxServerHttpExchange(ServerRequest request) {
    this.request = request;

    Flux<ByteBuffer> flux = Flux.create(chunkEmitter -> this.chunkEmitter = chunkEmitter);
    ConnectableFlux<ByteBuffer> chunks = flux.publish();
    chunks.connect();
    this.serverResponse = ServerResponse.ok().body((response, context) -> {
      this.response = response;
      return response.writeAndFlushWith(chunks.map(element -> Mono.just(response.bufferFactory().wrap(element))));
    });
  }

  Mono<ServerResponse> getServerResponse() {
    return serverResponse;
  }

  @Override
  public String uri() {
    URI uri = request.uri();
    return uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
  }

  @Override
  public HttpMethod method() {
    return HttpMethod.valueOf(request.methodName());
  }

  @Override
  public Set<String> headerNames() {
    return request.headers().asHttpHeaders().keySet();
  }

  @Override
  public List<String> headers(String name) {
    return request.headers().header(name);
  }

  @Override
  protected void doRead(Action<ByteBuffer> chunkAction) {
    request.bodyToFlux(ByteBuffer.class).subscribe(chunkAction::on, errorActions::fire, endActions::fire);
  }

  @Override
  protected void doSetStatus(HttpStatus status) {
    response.setStatusCode(org.springframework.http.HttpStatus.valueOf(status.code()));
  }

  @Override
  protected void doSetHeader(String name, String value) {
    response.getHeaders().set(name, value);
  }

  @Override
  protected void doWrite(ByteBuffer byteBuffer) {
    chunkEmitter.next(byteBuffer);
  }

  @Override
  protected void doEnd() {
    chunkEmitter.complete();
  }

  /**
   * {@link ServerRequest} and {@link ServerHttpResponse} are available.
   */
  @Override
  public <T> T unwrap(Class<T> clazz) {
    return ServerRequest.class.isAssignableFrom(clazz) ?
      clazz.cast(request) :
      ServerHttpResponse.class.isAssignableFrom(clazz) ?
        clazz.cast(response) :
        null;
  }

}
