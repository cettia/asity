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

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.DeferredContentProvider;

import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * This HTTP client opens an HTTP persistent connection to
 * <code>http://localhost:8080/echo</code>, sends elements of {@link HttpEchoClient#ECHO_QUEUE}
 * one by one by retrieving and removing the head of the queue when the server sends the received
 * data back, and close the connection when the queue becomes empty.
 *
 * @author Donghwan Kim
 */
public class HttpEchoClient {
  private static final String ECHO_URI = "http://localhost:8080/echo";
  private static final Queue<String> ECHO_QUEUE = new ConcurrentLinkedQueue<String>() {{
    add("AAAAA");
    add("BBBBB");
    add("CCCCC");
    add("DDDDD");
    add("EEEEE");
  }};

  public static void main(String... args) throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    HttpClient client = new HttpClient();
    client.start();

    DeferredContentProvider content = new DeferredContentProvider();
    client.POST(ECHO_URI).content(content, "text/plain; charset=utf-8").send(new Response.Listener.Adapter() {
      @Override
      public void onContent(Response response, ByteBuffer byteBuffer) {
        String data = ECHO_QUEUE.poll();
        if (data == null) {
          content.close();
        } else {
          content.offer(ByteBuffer.wrap(data.getBytes()));
        }
      }

      @Override
      public void onComplete(Result result) {
        latch.countDown();
      }
    });
    content.offer(ByteBuffer.wrap(ECHO_QUEUE.poll().getBytes()));

    latch.await();
    client.stop();
  }
}
