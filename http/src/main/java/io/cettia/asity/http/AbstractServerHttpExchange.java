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
package io.cettia.asity.http;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.SimpleActions;
import io.cettia.asity.action.VoidAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract base class for {@link ServerHttpExchange}.
 *
 * @author Donghwan Kim
 */
public abstract class AbstractServerHttpExchange implements ServerHttpExchange {

  protected final Actions<Void> endActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));
  protected final Actions<Throwable> errorActions = new SimpleActions<>();
  protected final Actions<Void> closeActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));

  private final Logger logger = LoggerFactory.getLogger(AbstractServerHttpExchange.class);
  private final Actions<Object> chunkActions = new SimpleActions<>();
  private final Actions<Object> bodyActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));
  private final Actions<Void> finishActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));
  private boolean read;
  private boolean readBody;
  private boolean ended;
  private String writeCharsetName = "ISO-8859-1";

  public AbstractServerHttpExchange() {
    endActions.add(new VoidAction() {
      @Override
      public void on() {
        logger.trace("{}'s request has ended", AbstractServerHttpExchange.this);
      }
    });
    finishActions.add(new VoidAction() {
      @Override
      public void on() {
        logger.trace("{}'s response has ended", AbstractServerHttpExchange.this);
      }
    });
    errorActions.add(new Action<Throwable>() {
      @Override
      public void on(Throwable throwable) {
        logger.trace("{} has received a throwable {}", AbstractServerHttpExchange.this, throwable);
      }
    });
    closeActions.add(new VoidAction() {
      @Override
      public void on() {
        logger.trace("{} has been closed", AbstractServerHttpExchange.this);
      }
    });
  }

  @Override
  public String header(String name) {
    List<String> headers = headers(name);
    return headers != null && headers.size() > 0 ? headers.get(0) : null;
  }

  @Override
  public ServerHttpExchange read() {
    if (!read) {
      if (hasTextBody()) {
        readAsText();
      } else {
        readAsBinary();
      }
    }
    return this;
  }

  private boolean hasTextBody() {
    // See http://www.w3.org/Protocols/rfc2616/rfc2616-sec7.html#sec7.2.1
    String contentType = header("content-type");
    return contentType != null && contentType.startsWith("text/");
  }

  @Override
  public ServerHttpExchange readAsText() {
    return readAsText(findCharsetName(header("content-type")));
  }

  private String findCharsetName(String contentType) {
    // HTTP 1.1 says that the default charset is ISO-8859-1
    // http://www.w3.org/International/O-HTTP-charset#charset
    String charsetName = "ISO-8859-1";
    if (contentType != null) {
      int idx = contentType.indexOf("charset=");
      if (idx != -1) {
        charsetName = contentType.substring(idx + "charset=".length());
      }
    }
    return charsetName;
  }

  @Override
  public ServerHttpExchange readAsText(String charsetName) {
    if (!read) {
      read = true;
      final Charset charset = Charset.forName(charsetName);
      doRead(new Action<ByteBuffer>() {
        @Override
        public void on(ByteBuffer byteBuffer) {
          chunkActions.fire(charset.decode(byteBuffer).toString());
        }
      });
    }
    return this;
  }

  @Override
  public ServerHttpExchange readAsBinary() {
    if (!read) {
      read = true;
      doRead(new Action<ByteBuffer>() {
        @Override
        public void on(ByteBuffer byteBuffer) {
          chunkActions.fire(byteBuffer);
        }
      });
    }
    return this;
  }

  protected abstract void doRead(Action<ByteBuffer> chunkAction);

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public ServerHttpExchange onchunk(Action action) {
    chunkActions.add(action);
    return this;
  }

  @Override
  public ServerHttpExchange onend(Action<Void> action) {
    endActions.add(action);
    return this;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public ServerHttpExchange onbody(Action action) {
    if (!readBody) {
      readBody = true;
      if (hasTextBody()) {
        final StringBuilder body = new StringBuilder();
        chunkActions.add(new Action<Object>() {
          @Override
          public void on(Object data) {
            body.append((String) data);
          }
        });
        endActions.add(new VoidAction() {
          @Override
          public void on() {
            bodyActions.fire(body.toString());
          }
        });
      } else {
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        chunkActions.add(new Action<Object>() {
          @Override
          public void on(Object data) {
            ByteBuffer byteBuffer = (ByteBuffer) data;
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            body.write(bytes, 0, bytes.length);
          }
        });
        endActions.add(new VoidAction() {
          @Override
          public void on() {
            bodyActions.fire(ByteBuffer.wrap(body.toByteArray()));
          }
        });
      }
    }
    bodyActions.add(action);
    return this;
  }

  @Override
  public ServerHttpExchange setStatus(HttpStatus status) {
    logger.trace("{} sets a response status, {}", this, status);
    doSetStatus(status);
    return this;
  }

  protected abstract void doSetStatus(HttpStatus status);

  @Override
  public final ServerHttpExchange setHeader(String name, Iterable<String> value) {
    // See http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
    Iterator<String> iterator = value.iterator();
    StringBuilder builder = new StringBuilder(iterator.next());
    while (iterator.hasNext()) {
      builder.append(", ").append(iterator.next());
    }
    return setHeader(name, builder.toString());
  }

  @Override
  public ServerHttpExchange setHeader(String name, String value) {
    logger.trace("{} sets a response header {} to {}", this, name, value);
    // Intercepts content-type header to find charset
    if (name.equalsIgnoreCase("content-type")) {
      writeCharsetName = findCharsetName(value);
    }
    doSetHeader(name, value);
    return this;
  }

  protected abstract void doSetHeader(String name, String value);

  @Override
  public ServerHttpExchange write(String data) {
    return write(data, writeCharsetName);
  }

  @Override
  public ServerHttpExchange write(String data, String charsetName) {
    logger.trace("{} sends a text chunk {} with charset {}", this, data, charsetName);
    doWrite(Charset.forName(charsetName).encode(data));
    return this;
  }

  @Override
  public ServerHttpExchange write(ByteBuffer byteBuffer) {
    logger.trace("{} sends a binary chunk {}", this, byteBuffer);
    doWrite(byteBuffer);
    return this;
  }

  protected abstract void doWrite(ByteBuffer byteBuffer);

  @Override
  public ServerHttpExchange end() {
    logger.trace("{} ends the response", this);
    if (!ended) {
      ended = true;
      doEnd();
      finishActions.fire();
    }
    return this;
  }

  protected abstract void doEnd();

  @Override
  public ServerHttpExchange end(String data) {
    return write(data).end();
  }

  @Override
  public ServerHttpExchange end(String data, String charsetName) {
    return write(data, charsetName).end();
  }

  @Override
  public ServerHttpExchange end(ByteBuffer data) {
    return write(data).end();
  }

  @Override
  public ServerHttpExchange onfinish(Action<Void> action) {
    finishActions.add(action);
    return this;
  }

  @Override
  public ServerHttpExchange onclose(Action<Void> action) {
    closeActions.add(action);
    return this;
  }

  @Override
  public ServerHttpExchange onerror(Action<Throwable> action) {
    errorActions.add(action);
    return this;
  }

}
