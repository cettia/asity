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
package io.cettia.asity.bridge.grizzly2;

import io.cettia.asity.action.Action;
import io.cettia.asity.http.AbstractServerHttpExchange;
import io.cettia.asity.http.HttpMethod;
import io.cettia.asity.http.HttpStatus;
import io.cettia.asity.http.ServerHttpExchange;
import org.glassfish.grizzly.CloseListener;
import org.glassfish.grizzly.Closeable;
import org.glassfish.grizzly.ICloseType;
import org.glassfish.grizzly.ReadHandler;
import org.glassfish.grizzly.http.io.NIOInputStream;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link ServerHttpExchange} for Grizzly 2.
 *
 * @author Donghwan Kim
 */
public class GrizzlyServerHttpExchange extends AbstractServerHttpExchange {

  private final Request request;
  private final Response response;

  @SuppressWarnings("deprecation")
  public GrizzlyServerHttpExchange(Request request, Response response) {
    this.request = request;
    this.response = response;
    request.getContext().getConnection().addCloseListener(new CloseListener<Closeable,
      ICloseType>() {
      @Override
      public void onClosed(Closeable closeable, ICloseType type) throws
        IOException {
        closeActions.fire();
      }
    });
    // To detect closed connection
    // From https://github.com/GrizzlyNIO/grizzly-mirror/blob/2_3_17/modules/comet/src/main/java
    // /org/glassfish/grizzly/comet/CometContext.java#L250
    request.getInputBuffer().initiateAsyncronousDataReceiving();
    response.suspend();
  }

  @Override
  public String uri() {
    String uri = request.getRequestURI();
    if (request.getQueryString() != null) {
      uri += "?" + request.getQueryString();
    }
    return uri;
  }

  @Override
  public HttpMethod method() {
    return HttpMethod.valueOf(request.getMethod().getMethodString());
  }

  @Override
  public Set<String> headerNames() {
    Set<String> headerNames = new LinkedHashSet<>();
    for (String headerName : request.getHeaderNames()) {
      headerNames.add(headerName);
    }
    return headerNames;
  }

  @Override
  public List<String> headers(String name) {
    List<String> headers = new ArrayList<String>();
    for (String header : request.getHeaders(name)) {
      headers.add(header);
    }
    return headers;
  }

  @Override
  protected void doRead(final Action<ByteBuffer> chunkAction) {
    final NIOInputStream in = request.getNIOInputStream();
    in.notifyAvailable(new ReadHandler() {
      @Override
      public void onDataAvailable() throws Exception {
        int bytesRead = -1;
        byte buffer[] = new byte[8192];
        while (in.isReady() && (bytesRead = in.read(buffer)) != -1) {
          chunkAction.on(ByteBuffer.wrap(buffer, 0, bytesRead));
        }
      }

      @Override
      public void onAllDataRead() throws Exception {
        // Unlike Servlet 3.1, there may be remaining data
        onDataAvailable();
        endActions.fire();
      }

      @Override
      public void onError(Throwable t) {
        errorActions.fire(t);
      }
    });
  }

  @Override
  protected void doSetStatus(HttpStatus status) {
    response.setStatus(status.code(), status.reason());
  }

  @Override
  protected void doSetHeader(String name, String value) {
    response.setHeader(name, value);
  }

  @Override
  protected void doWrite(ByteBuffer byteBuffer) {
    try {
      byte[] bytes = new byte[byteBuffer.remaining()];
      byteBuffer.get(bytes);
      OutputStream out = response.getNIOOutputStream();
      out.write(bytes);
      out.flush();
    } catch (IOException e) {
      errorActions.fire(e);
    }
  }

  @Override
  protected void doEnd() {
    response.resume();
  }

  @Override
  public <T> T unwrap(Class<T> clazz) {
    return Request.class.isAssignableFrom(clazz) ?
      clazz.cast(request) :
      Response.class.isAssignableFrom(clazz) ?
        clazz.cast(response) :
        null;
  }

}
