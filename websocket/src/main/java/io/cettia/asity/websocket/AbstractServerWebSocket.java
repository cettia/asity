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
package io.cettia.asity.websocket;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.SimpleActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Abstract base class for {@link ServerWebSocket}.
 *
 * @author Donghwan Kim
 */
public abstract class AbstractServerWebSocket implements ServerWebSocket {

  protected final Actions<String> textActions = new SimpleActions<>();
  protected final Actions<ByteBuffer> binaryActions = new SimpleActions<>();
  protected final Actions<Throwable> errorActions = new SimpleActions<>();
  protected final Actions<Void> closeActions = new SimpleActions<>(new Actions.Options().once(true).memory(true));

  private final Logger logger = LoggerFactory.getLogger(AbstractServerWebSocket.class);
  private State state = State.OPEN;

  public AbstractServerWebSocket() {
    closeActions.add($ -> state = State.CLOSED);
    if (logger.isDebugEnabled()) {
      textActions.add(frame -> logger.debug("{} receives a text frame {}", this, frame));
      binaryActions.add(frame -> logger.debug("{} receives a binary frame {}", this, frame));
      errorActions.add(throwable -> logger.debug("{} has received a throwable {}", this, throwable));
      closeActions.add($ -> logger.debug("{} has been closed", this));
    }
  }

  @Override
  public String header(String name) {
    List<String> headers = headers(name);
    return headers != null && headers.size() > 0 ? headers.get(0) : null;
  }

  @Override
  public void close() {
    if (logger.isDebugEnabled()) {
      logger.debug("{} has started to close the connection", this);
    }
    if (state != State.CLOSING && state != State.CLOSED) {
      state = State.CLOSING;
      doClose();
    }
  }

  protected abstract void doClose();

  @Override
  public ServerWebSocket send(String data) {
    if (logger.isDebugEnabled()) {
      logger.debug("{} sends a text frame {}", this, data);
    }
    doSend(data);
    return this;
  }

  @Override
  public ServerWebSocket send(ByteBuffer byteBuffer) {
    if (logger.isDebugEnabled()) {
      logger.debug("{} sends a binary frame {}", this, byteBuffer);
    }
    doSend(byteBuffer);
    return this;
  }

  protected abstract void doSend(ByteBuffer byteBuffer);

  protected abstract void doSend(String data);

  @Override
  public ServerWebSocket ontext(Action<String> action) {
    textActions.add(action);
    return this;
  }

  @Override
  public ServerWebSocket onbinary(Action<ByteBuffer> action) {
    binaryActions.add(action);
    return this;
  }

  @Override
  public ServerWebSocket onclose(Action<Void> action) {
    closeActions.add(action);
    return this;
  }

  @Override
  public ServerWebSocket onerror(Action<Throwable> action) {
    errorActions.add(action);
    return this;
  }

  @Override
  public String toString() {
    return String.format("%s@%x[state=%s]", getClass().getSimpleName(), hashCode(), state);
  }

  /**
   * Represents the state of the connection.
   *
   * @author Donghwan Kim
   * @see <a
   * href="http://www.w3.org/TR/websockets/#dom-websocket-readystate">The
   * WebSocket API by W3C - The readyState attribute</a>
   */
  private enum State {

    /**
     * The connection has not yet been established.
     */
    CONNECTING,

    /**
     * The WebSocket connection is established and communication is possible.
     */
    OPEN,

    /**
     * The close() method has been invoked.
     */
    CLOSING,

    /**
     * The connection has been closed or could not be opened.
     */
    CLOSED

  }

}
