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
package io.cettia.asity.http;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.SimpleActions;
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

  // HTTP 1.1 says that the default charset is ISO-8859-1
  // http://www.w3.org/International/O-HTTP-charset#charset
  private static final String DEFAULT_CHARSET_NAME = "ISO-8859-1";

  protected final Actions<Void> endActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));
  protected final Actions<Throwable> errorActions = new SimpleActions<>();
  protected final Actions<Void> closeActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));

  private final Logger logger = LoggerFactory.getLogger(AbstractServerHttpExchange.class);
  private final Actions<Object> chunkActions = new SimpleActions<>();
  private final Actions<Object> bodyActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));
  private final Actions<Void> finishActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));
  private String writeCharsetName = DEFAULT_CHARSET_NAME;

  // Request state
  private boolean reading;
  private boolean readingBody;
  private boolean requestEnded;
  // Response state
  private boolean writing;
  private boolean responseEnded;

  public AbstractServerHttpExchange() {
    endActions.add($ -> requestEnded = true);
    if (logger.isDebugEnabled()) {
      endActions.add($ -> logger.debug("{} request has ended", this));
      finishActions.add($ -> logger.debug("{} response has ended", this));
      errorActions.add(throwable -> logger.debug("{} has received a throwable {}", this, throwable));
      closeActions.add($ -> logger.debug("{} has been aborted", this));
    }
  }

  @Override
  public String header(String name) {
    List<String> headers = headers(name);
    return headers != null && headers.size() > 0 ? headers.get(0) : null;
  }

  @Override
  public ServerHttpExchange read() {
    if (!reading) {
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
    String charsetName = DEFAULT_CHARSET_NAME;
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
    if (!reading) {
      reading = true;
      final Charset charset = Charset.forName(charsetName);
      doRead(byteBuffer -> {
        String chunk = charset.decode(byteBuffer).toString();
        if (logger.isDebugEnabled()) {
          logger.debug("{} reads a text chunk {} with charset {}", this, chunk, charsetName);
        }
        chunkActions.fire(chunk);
      });
    }
    return this;
  }

  @Override
  public ServerHttpExchange readAsBinary() {
    if (!reading) {
      reading = true;
      doRead(byteBuffer -> {
        if (logger.isDebugEnabled()) {
          logger.debug("{} reads a binary chunk {}", this, byteBuffer);
        }
        chunkActions.fire(byteBuffer);
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
    if (!readingBody) {
      readingBody = true;
      if (hasTextBody()) {
        final StringBuilder body = new StringBuilder();
        chunkActions.add(data -> body.append((String) data));
        endActions.add($ -> bodyActions.fire(body.toString()));
      } else {
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        chunkActions.add(data -> {
          ByteBuffer byteBuffer = (ByteBuffer) data;
          byte[] bytes = new byte[byteBuffer.remaining()];
          byteBuffer.get(bytes);
          body.write(bytes, 0, bytes.length);
        });
        endActions.add($ -> bodyActions.fire(ByteBuffer.wrap(body.toByteArray())));
      }
    }
    bodyActions.add(action);
    return this;
  }

  @Override
  public ServerHttpExchange setStatus(HttpStatus status) {
    if (logger.isDebugEnabled()) {
      logger.debug("{} sets a response status {}", this, status);
    }
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
    if (logger.isDebugEnabled()) {
      logger.debug("{} sets a response header {} to {}", this, name, value);
    }
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
    writing = true;
    if (logger.isDebugEnabled()) {
      logger.debug("{} writes a text chunk {} with charset {}", this, data, charsetName);
    }
    doWrite(Charset.forName(charsetName).encode(data));
    return this;
  }

  @Override
  public ServerHttpExchange write(ByteBuffer byteBuffer) {
    writing = true;
    if (logger.isDebugEnabled()) {
      logger.debug("{} writes a binary chunk {}", this, byteBuffer);
    }
    doWrite(byteBuffer);
    return this;
  }

  protected abstract void doWrite(ByteBuffer byteBuffer);

  @Override
  public ServerHttpExchange end() {
    if (!responseEnded) {
      responseEnded = true;
      if (logger.isDebugEnabled()) {
        logger.debug("{} ends the response", this);
      }
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

  @Override
  public String toString() {
    String requestState = requestEnded ? "ENDED" : reading ? "READING" : "UNREAD";
    String responseState = responseEnded ? "ENDED" : writing ? "WRITING" : "UNWRITTEN";

    return String.format("%s@%x[request=%s,response=%s]", getClass().getSimpleName(),
      hashCode(), requestState, responseState);
  }

}
