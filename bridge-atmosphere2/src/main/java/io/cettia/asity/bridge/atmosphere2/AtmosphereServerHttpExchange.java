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
package io.cettia.asity.bridge.atmosphere2;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.http.AbstractServerHttpExchange;
import io.cettia.asity.http.HttpMethod;
import io.cettia.asity.http.HttpStatus;
import io.cettia.asity.http.ServerHttpExchange;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResourceEventListenerAdapter;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link ServerHttpExchange} for Atmosphere 2.
 *
 * @author Donghwan Kim
 */
public class AtmosphereServerHttpExchange extends AbstractServerHttpExchange {

  private final AtmosphereResource resource;
  private final AtmosphereResponse response;
  private final AtmosphereRequest request;

  public AtmosphereServerHttpExchange(AtmosphereResource resource) {
    this.resource = resource.suspend();
    // Prevent IllegalStateException when the connection gets closed.
    this.response = AtmosphereResourceImpl.class.cast(resource).getResponse(false);
    this.request = AtmosphereResourceImpl.class.cast(resource).getRequest(false);
    resource.addEventListener(new AtmosphereResourceEventListenerAdapter() {
      @Override
      public void onDisconnect(AtmosphereResourceEvent event) {
        closeActions.fire();
      }

      @Override
      public void onClose(AtmosphereResourceEvent event) {
        closeActions.fire();
      }

      @Override
      public void onThrowable(AtmosphereResourceEvent event) {
        errorActions.fire(event.throwable());
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

  @SuppressWarnings("unchecked")
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
    resource.resume();
  }

  /**
   * {@link AtmosphereResource} is available.
   */
  @Override
  public <T> T unwrap(Class<T> clazz) {
    return AtmosphereResource.class.isAssignableFrom(clazz) ? clazz.cast(resource) : null;
  }

  private abstract static class BodyReader {
    final ServletInputStream input;
    final Action<ByteBuffer> chunkAction;
    final Actions<Void> endActions;
    final Actions<Throwable> errorActions;

    public BodyReader(ServletInputStream input, Action<ByteBuffer> chunkAction, Actions<Void> endActions, Actions<Throwable> errorActions) {
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
    public AsyncBodyReader(ServletInputStream input, Action<ByteBuffer> action, Actions<Void> endActions, Actions<Throwable> errorActions) {
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
    public SyncBodyReader(ServletInputStream input, Action<ByteBuffer> action, Actions<Void> endActions, Actions<Throwable> errorActions) {
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
      })
        .start();
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
