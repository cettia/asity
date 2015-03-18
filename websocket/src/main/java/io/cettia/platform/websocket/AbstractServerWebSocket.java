/*
 * Copyright 2015 The Cettia Project
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
package io.cettia.platform.websocket;

import io.cettia.platform.action.Action;
import io.cettia.platform.action.Actions;
import io.cettia.platform.action.SimpleActions;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        errorActions.add(new Action<Throwable>() {
            @Override
            public void on(Throwable throwable) {
                logger.trace("{} has received a throwable {}", AbstractServerWebSocket.this, throwable);
            }
        });
        closeActions.add(new Action<Void>() {
            @Override
            public void on(Void _) {
                state = State.CLOSED;
                logger.trace("{} has been closed", AbstractServerWebSocket.this);
            }
        });
    }

    @Override
    public void close() {
        logger.trace("{} has started to close the connection", this);
        if (state != State.CLOSING && state != State.CLOSED) {
            state = State.CLOSING;
            doClose();
        }
    }

    protected abstract void doClose();

    @Override
    public ServerWebSocket send(String data) {
        logger.trace("{} sends a text message {}", this, data);
        doSend(data);
        return this;
    }

    @Override
    public ServerWebSocket send(ByteBuffer byteBuffer) {
        if (logger.isTraceEnabled() && byteBuffer.hasArray()) {
            logger.trace("{} sends a text message {}", this, new String(byteBuffer.array()));
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

    /**
     * Represents the state of the connection.
     *
     * @author Donghwan Kim
     * @see <a
     *      href="http://www.w3.org/TR/websockets/#dom-websocket-readystate">The
     *      WebSocket API by W3C - The readyState attribute</a>
     */
    private static enum State {

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
