/*
 * Copyright 2015 the original author or authors.
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
package io.cettia.asity.bridge.netty4;

import io.cettia.asity.action.Action;
import io.cettia.asity.http.AbstractServerHttpExchange;
import io.cettia.asity.http.HttpMethod;
import io.cettia.asity.http.HttpStatus;
import io.cettia.asity.http.ServerHttpExchange;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * {@link ServerHttpExchange} for Netty 4.
 *
 * @author Donghwan Kim
 */
public class NettyServerHttpExchange extends AbstractServerHttpExchange {

  private final ChannelHandlerContext context;
  private final HttpRequest request;
  private final HttpResponse response;
  private boolean written;
  private Action<ByteBuffer> chunkAction;

  public NettyServerHttpExchange(ChannelHandlerContext context, HttpRequest request) {
    this.context = context;
    this.request = request;
    this.response = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK,
      false);
    response.headers().set(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
  }

  void handleError(Throwable cause) {
    errorActions.fire(cause);
  }

  void handleClose() {
    closeActions.fire();
  }

  @Override
  public String uri() {
    return request.getUri();
  }

  @Override
  public HttpMethod method() {
    return HttpMethod.valueOf(request.getMethod().toString());
  }

  @Override
  public Set<String> headerNames() {
    return request.headers().names();
  }

  @Override
  public List<String> headers(String name) {
    return request.headers().getAll(name);
  }

  @Override
  protected void doRead(Action<ByteBuffer> chunkAction) {
    this.chunkAction = chunkAction;
  }

  void handleChunk(HttpContent chunk) {
    // To obtain chunkAction
    read();
    ByteBuf buf = chunk.content();
    if (buf.isReadable() && this.chunkAction != null) {
      this.chunkAction.on(buf.nioBuffer());
    }
    if (chunk instanceof LastHttpContent) {
      endActions.fire();
    }
  }

  @Override
  protected void doSetStatus(HttpStatus status) {
    response.setStatus(new HttpResponseStatus(status.code(), status.reason()));
  }

  @Override
  protected void doSetHeader(String name, String value) {
    response.headers().set(name, value);
  }

  @Override
  protected void doWrite(ByteBuffer byteBuffer) {
    ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(byteBuffer));
    if (!written) {
      written = true;
      context.write(response);
    }
    context.writeAndFlush(buf);
  }

  @Override
  protected void doEnd() {
    if (!written) {
      written = true;
      context.write(response);
    }
    context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
  }

  @Override
  public <T> T unwrap(Class<T> clazz) {
    return ChannelHandlerContext.class.isAssignableFrom(clazz) ?
      clazz.cast(context) :
      HttpRequest.class.isAssignableFrom(clazz) ?
        clazz.cast(request) :
        HttpResponse.class.isAssignableFrom(clazz) ?
          clazz.cast(response) :
          null;
  }

}
