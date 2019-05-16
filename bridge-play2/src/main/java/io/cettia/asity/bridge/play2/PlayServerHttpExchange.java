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

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.Status;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.SimpleActions;
import io.cettia.asity.http.AbstractServerHttpExchange;
import io.cettia.asity.http.HttpMethod;
import io.cettia.asity.http.HttpStatus;
import io.cettia.asity.http.ServerHttpExchange;
import play.http.HttpEntity;
import play.mvc.Http;
import play.mvc.Result;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * {@link ServerHttpExchange} for Play framework 2.
 *
 * @author Donghwan Kim
 */
public class PlayServerHttpExchange extends AbstractServerHttpExchange {

  private final Http.Request request;
  private final CompletableFuture<Result> resultFuture = new CompletableFuture<>();
  private HttpStatus status = HttpStatus.OK;
  private final Map<String, String> responseHeaders = new LinkedHashMap<>();
  private final Actions<ActorRef> actorActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));
  private boolean flushed;

  public PlayServerHttpExchange(Http.Request request) {
    this.request = request;
  }

  CompletableFuture<Result> getResultFuture() {
    return resultFuture;
  }

  @Override
  public String uri() {
    return request.uri();
  }

  @Override
  public HttpMethod method() {
    return HttpMethod.valueOf(request.method());
  }

  @Override
  public Set<String> headerNames() {
    return request.getHeaders().toMap().keySet();
  }

  @Override
  public List<String> headers(String name) {
    return request.getHeaders().getAll(name);
  }


  @Override
  protected void doRead(Action<ByteBuffer> chunkAction) {
    chunkAction.on((request.body().asBytes().toByteBuffer()));
    endActions.fire();
  }

  @Override
  protected void doSetStatus(HttpStatus status) {
    this.status = status;
  }

  @Override
  protected void doSetHeader(String name, String value) {
    responseHeaders.put(name, value);
  }

  private void flushStatusAndHeaders() {
    Source<ByteString, ?> chunks = Source.<ByteString>actorRef(256, OverflowStrategy.fail()).mapMaterializedValue(actor -> {
      this.actorActions.fire(actor);
      return NotUsed.getInstance();
    });
    Result result = new Result(status.code(), status.reason(), responseHeaders, HttpEntity.chunked(chunks, Optional.empty()));
    resultFuture.complete(result);
  }

  @Override
  protected void doWrite(ByteBuffer byteBuffer) {
    if (!flushed) {
      flushed = true;
      flushStatusAndHeaders();
    }

    actorActions.add(actor -> actor.tell(ByteString.fromByteBuffer(byteBuffer), null));
  }

  @Override
  protected void doEnd() {
    if (!flushed) {
      flushed = true;
      flushStatusAndHeaders();
    }

    actorActions.add(actor -> actor.tell(new Status.Success(NotUsed.getInstance()), null));
  }

  /**
   * {@link Http.Request} are available.
   */
  @Override
  public <T> T unwrap(Class<T> clazz) {
    return Http.Request.class.isAssignableFrom(clazz) ?
      clazz.cast(request) :
      null;
  }

}
