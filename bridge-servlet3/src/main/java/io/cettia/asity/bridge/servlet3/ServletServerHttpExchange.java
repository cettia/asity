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
package io.cettia.asity.bridge.servlet3;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.http.AbstractServerHttpExchange;
import io.cettia.asity.http.HttpMethod;
import io.cettia.asity.http.HttpStatus;
import io.cettia.asity.http.ServerHttpExchange;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link ServerHttpExchange} for Servlet 3.
 *
 * @author Donghwan Kim
 */
public class ServletServerHttpExchange extends AbstractServerHttpExchange {

  private final HttpServletRequest request;
  private final HttpServletResponse response;

  public ServletServerHttpExchange(HttpServletRequest request, HttpServletResponse response) {
    this.request = request;
    this.response = response;
    AsyncContext async = request.startAsync();
    async.setTimeout(0);
    async.addListener(new AsyncListener() {
      @Override
      public void onStartAsync(AsyncEvent event) throws IOException {
      }

      @Override
      public void onComplete(AsyncEvent event) throws IOException {
      }

      @Override
      public void onTimeout(AsyncEvent event) throws IOException {
        closeActions.fire();
      }

      @Override
      public void onError(AsyncEvent event) throws IOException {
        errorActions.fire(event.getThrowable());
      }
    });
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
    return HttpMethod.valueOf(request.getMethod());
  }

  @Override
  public Set<String> headerNames() {
    Set<String> headerNames = new LinkedHashSet<>();
    Enumeration<String> enumeration = request.getHeaderNames();
    while (enumeration.hasMoreElements()) {
      headerNames.add(enumeration.nextElement());
    }
    return headerNames;
  }

  @Override
  public List<String> headers(String name) {
    return Collections.list(request.getHeaders(name));
  }

  @Override
  protected void doRead(Action<ByteBuffer> chunkAction) {
    try {
      ServletInputStream input = request.getInputStream();
      int version = getServletMinorVersion();
      if (version > 0) {
        // 3.1+ asynchronous
        new AsyncBodyReader(input, chunkAction, endActions, errorActions);
      } else {
        // 3.0 synchronous
        new SyncBodyReader(input, chunkAction, endActions, errorActions);
      }
    } catch (IOException e) {
      errorActions.fire(e);
    }
  }

  private int getServletMinorVersion() {
    int version = request.getServletContext().getMinorVersion();
    // Some implementations returns 0 even though they implement 3.1
    if (version == 0) {
      String info = request.getServletContext().getServerInfo();
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=448761
      if (info.startsWith("jetty/9.1") || info.startsWith("jetty/9.2")) {
        version = 1;
      }
    }
    return version;
  }

  @Override
  protected void doSetStatus(HttpStatus status) {
    response.setStatus(status.code());
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
      OutputStream outputStream = response.getOutputStream();
      outputStream.write(bytes);
      outputStream.flush();
    } catch (IOException e) {
      errorActions.fire(e);
    }
  }

  @Override
  protected void doEnd() {
    request.getAsyncContext().complete();
  }

  /**
   * {@link HttpServletRequest} and {@link HttpServletResponse} are available.
   */
  @Override
  public <T> T unwrap(Class<T> clazz) {
    return HttpServletRequest.class.isAssignableFrom(clazz) ?
      clazz.cast(request) :
      HttpServletResponse.class.isAssignableFrom(clazz) ?
        clazz.cast(response) :
        null;
  }

  private abstract static class BodyReader {
    final ServletInputStream input;
    final Action<ByteBuffer> chunkAction;
    final Actions<Void> endActions;
    final Actions<Throwable> errorActions;

    public BodyReader(ServletInputStream input, Action<ByteBuffer> chunkAction, Actions<Void>
      endActions, Actions<Throwable> errorActions) {
      this.input = input;
      this.chunkAction = chunkAction;
      this.endActions = endActions;
      this.errorActions = errorActions;
      start();
    }

    abstract void start();

    void read() throws IOException {
      int bytesRead = -1;
      byte buffer[] = new byte[8192];
      while (ready() && (bytesRead = input.read(buffer)) != -1) {
        chunkAction.on(ByteBuffer.wrap(buffer, 0, bytesRead));
      }
    }

    abstract boolean ready();

    void end() {
      endActions.fire();
    }
  }

  private static class AsyncBodyReader extends BodyReader {
    public AsyncBodyReader(ServletInputStream input, Action<ByteBuffer> action, Actions<Void>
      endActions, Actions<Throwable> errorActions) {
      super(input, action, endActions, errorActions);
    }

    @Override
    void start() {
      input.setReadListener(new ReadListener() {
        @Override
        public void onDataAvailable() throws IOException {
          read();
        }

        @Override
        public void onAllDataRead() throws IOException {
          end();
        }

        @Override
        public void onError(Throwable t) {
          errorActions.fire(t);
        }
      });
    }

    @Override
    boolean ready() {
      return input.isReady();
    }
  }

  private static class SyncBodyReader extends BodyReader {
    public SyncBodyReader(ServletInputStream input, Action<ByteBuffer> action, Actions<Void>
      endActions, Actions<Throwable> errorActions) {
      super(input, action, endActions, errorActions);
    }

    @Override
    void start() {
      new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            read();
            end();
          } catch (IOException e) {
            errorActions.fire(e);
          }
        }
      }).start();
    }

    @Override
    boolean ready() {
      try {
        return input.available() > 0;
      } catch (IOException e) {
        errorActions.fire(e);
        return false;
      }
    }
  }

}
