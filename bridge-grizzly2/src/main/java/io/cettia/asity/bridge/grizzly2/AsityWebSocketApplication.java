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
package io.cettia.asity.bridge.grizzly2;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.websocket.ServerWebSocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;

/**
 * WebSocketApplication to process {@link WebSocket} into
 * {@link GrizzlyServerWebSocket}.
 * <p>
 * 
 * <pre>
 * NetworkListener listener = httpServer.getListener("grizzly");
 * listener.registerAddOn(new WebSocketAddOn());
 * WebSocketEngine.getEngine().register("", "/cettia", new AsityWebSocketApplication().onwebsocket(ws -&gt {}));
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityWebSocketApplication extends WebSocketApplication {
    
    private Actions<ServerWebSocket> wsActions = new ConcurrentActions<>();
    private Map<WebSocket, GrizzlyServerWebSocket> sockets = new ConcurrentHashMap<>();

    @Override
    public void onConnect(WebSocket socket) {
        super.onConnect(socket);
        GrizzlyServerWebSocket ws = new GrizzlyServerWebSocket((DefaultWebSocket) socket);
        sockets.put(socket, ws);
        wsActions.fire(ws);
    }

    
    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        super.onClose(socket, frame);
        sockets.remove(socket);
    }

    @Override
    protected boolean onError(WebSocket webSocket, Throwable t) {
        boolean ret = super.onError(webSocket, t);
        sockets.get(webSocket).onError(t);
        return ret;
    }

    /**
     * Registers an action to be called when {@link ServerWebSocket} is
     * available.
     */
    public AsityWebSocketApplication onwebsocket(Action<ServerWebSocket> action) {
        wsActions.add(action);
        return this;
    }

}
