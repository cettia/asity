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
package io.cettia.asity.example.echo;

import io.cettia.asity.action.Action;
import io.cettia.asity.websocket.ServerWebSocket;

import java.nio.ByteBuffer;

/**
 * @author Donghwan Kim
 */
public class WebSocketEchoServer implements Action<ServerWebSocket> {
  @Override
  public void on(ServerWebSocket ws) {
    // Reads handshake request URI and headers
    System.out.println(ws.uri());
    ws.headerNames().stream().forEach(name -> System.out.println(name + ": " + String.join(", ", ws.headers(name))));

    // Sends the received text frame and binary frame back
    ws.ontext((String text) -> ws.send(text)).onbinary((ByteBuffer binary) -> ws.send(binary));

    // Exception handling
    ws.onerror((Throwable t) -> t.printStackTrace());
  }
}
