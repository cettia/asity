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
package io.cettia.asity.bridge.play2;

import io.cettia.asity.action.Action;
import io.cettia.asity.websocket.AbstractServerWebSocket;
import io.cettia.asity.websocket.ServerWebSocket;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.mvc.Http.Request;
import play.mvc.WebSocket;
import play.mvc.WebSocket.In;
import play.mvc.WebSocket.Out;

/**
 * {@link ServerWebSocket} for Play 2.
 *
 * @author Donghwan Kim
 */
public class PlayServerWebSocket extends AbstractServerWebSocket {

    private final Logger log = LoggerFactory.getLogger(PlayServerWebSocket.class);
    private final Request request;
    private final WebSocket.Out<String> out;

    public PlayServerWebSocket(Request request, In<String> in, Out<String> out) {
        this.request = request;
        this.out = out;
        // Supports text frame only for now due to limitations of Play Java API
        in.onMessage(new Callback<String>() {
            @Override
            public void invoke(String message) throws Throwable {
                textActions.fire(message);
            }
        });
        in.onClose(new Callback0() {
            @Override
            public void invoke() throws Throwable {
                closeActions.fire();
            }
        });
    }
    
    @Override
    public ServerWebSocket onbinary(Action<ByteBuffer> action) {
        log.error("Play Java API doesn't allow to receive text and binary frame together in a single connection");
        return this;
    }

    @Override
    public String uri() {
        return request.uri();
    }

    @Override
    protected void doClose() {
        out.close();
    }

    @Override
    protected void doSend(String data) {
        out.write(data);
    }

    @Override
    protected void doSend(ByteBuffer byteBuffer) {
        // Transformation from binary to text results in sending a text frame not binary frame
        log.error("Play Java API doesn't allow to send text and binary frame together in a single connection");
    }

    /**
     * {@link Request} and {@link WebSocket.Out} are available.
     */
    @Override
    public <T> T unwrap(Class<T> clazz) {
        return Request.class.isAssignableFrom(clazz) ?
                clazz.cast(request) :
                Out.class.isAssignableFrom(clazz) ?
                        clazz.cast(out) :
                        null;
    }

}
