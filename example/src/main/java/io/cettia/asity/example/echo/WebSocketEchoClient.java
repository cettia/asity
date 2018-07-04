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

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;


/**
 * This WebSocket client opens a WebSocket connection to <code>ws://localhost:8080/echo</code>,
 * sends elements of {@link WebSocketEchoClient#ECHO_QUEUE} one by one by retrieving and removing
 * the head of the queue when the server sends the received data back, and close the connection
 * when the queue becomes empty.
 *
 * @author Donghwan Kim
 */
public class WebSocketEchoClient {
  private static final String ECHO_URI = "ws://localhost:8080/echo";
  private static final Queue<String> ECHO_QUEUE = new ConcurrentLinkedQueue<String>() {{
    add("AAAAA");
    add("BBBBB");
    add("CCCCC");
    add("DDDDD");
    add("EEEEE");
  }};

  public static void main(String... args) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    WebSocketClient client = new WebSocketClient();
    client.start();

    client.connect(new WebSocketAdapter() {
      @Override
      public void onWebSocketConnect(Session session) {
        super.onWebSocketConnect(session);
        getRemote().sendStringByFuture(ECHO_QUEUE.poll());
      }

      @Override
      public void onWebSocketText(String message) {
        String data = ECHO_QUEUE.poll();
        if (data == null) {
          getSession().close();
        } else {
          getRemote().sendStringByFuture(data);
        }
      }

      @Override
      public void onWebSocketClose(int statusCode, String reason) {
        latch.countDown();
      }
    }, URI.create(ECHO_URI));

    latch.await();
    client.stop();
  }
}
