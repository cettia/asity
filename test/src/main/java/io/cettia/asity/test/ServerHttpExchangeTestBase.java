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
package io.cettia.asity.test;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.VoidAction;
import io.cettia.asity.http.HttpStatus;
import io.cettia.asity.http.ServerHttpExchange;
import net.jodah.concurrentunit.ConcurrentTestCase;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.ByteArrayOutputStream;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * @author Donghwan Kim
 */
public abstract class ServerHttpExchangeTestBase extends ConcurrentTestCase {

  protected static final String TEST_URI = "/test";
  private static final CompleteListener ASYNC = new Response.Listener.Adapter();

  @Rule
  public Timeout globalTimeout = Timeout.seconds(30);
  protected HttpClient client = new HttpClient();

  private int port;
  private Action<ServerHttpExchange> requestAction;

  @Before
  public void setUp() throws Exception {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      port = serverSocket.getLocalPort();
    }
    client.start();
    startServer(port, new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        requestAction.on(http);
      }
    });
  }

  @After
  public void tearDown() throws Exception {
    stopServer();
    client.stop();
    requestAction = null;
  }

  protected abstract void startServer(int port, Action<ServerHttpExchange> requestAction) throws
    Exception;

  protected abstract void stopServer() throws Exception;

  protected String uri() {
    return uri(TEST_URI);
  }

  protected String uri(String path) {
    return "http://localhost:" + port + path;
  }

  protected void requestAction(Action<ServerHttpExchange> requestAction) {
    this.requestAction = requestAction;
  }

  @Test
  public void testURI() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        threadAssertEquals(http.uri(), "/test?hello=there");
        resume();
      }
    });
    client.newRequest(uri("/test?hello=there")).send(ASYNC);
    await();
  }

  @Test
  public void testMethod() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        threadAssertEquals(http.method(), io.cettia.asity.http.HttpMethod.POST);
        resume();
      }
    });
    client.newRequest(uri()).method(HttpMethod.POST).send(ASYNC);
    await();
  }

  @Test
  public void testHeader() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        threadAssertTrue(http.headerNames().containsAll(Arrays.asList("a", "b"))
          || http.headerNames().containsAll(Arrays.asList("A", "B")));
        threadAssertEquals(http.header("A"), "A");
        threadAssertEquals(http.header("B"), "B1");
        threadAssertTrue(http.headers("A").containsAll(Arrays.asList("A")));
        threadAssertTrue(http.headers("B").containsAll(Arrays.asList("B1", "B2")));
        resume();
      }
    });
    client.newRequest(uri()).header("A", "A").header("B", "B1").header("B", "B2").send(ASYNC);
    await();
  }

  @Test
  public void testReadText() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        final StringBuilder body = new StringBuilder();
        http.onchunk(new Action<String>() {
          @Override
          public void on(String data) {
            body.append(data);
          }
        })
        .onend(new VoidAction() {
          @Override
          public void on() {
            threadAssertEquals(body.toString(), "A Breath Clad In Happiness");
            resume();
          }
        })
        .read();
      }
    });
    client.newRequest(uri()).method(HttpMethod.POST)
    .content(new StringContentProvider("A Breath Clad In Happiness"), "text/plain; charset=utf-8")
    .send(ASYNC);
    await();
  }

  @Test
  public void testReadAsText() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        final StringBuilder body = new StringBuilder();
        http.onchunk(new Action<String>() {
          @Override
          public void on(String data) {
            body.append(data);
          }
        })
        .onend(new VoidAction() {
          @Override
          public void on() {
            threadAssertEquals(body.toString(), "Day 7: Poem of the Ocean");
            resume();
          }
        })
        .readAsText();
      }
    });
    client.newRequest(uri()).method(HttpMethod.POST)
    .content(new StringContentProvider("Day 7: Poem of the Ocean"), "application/octet-stream")
    .send(ASYNC);
    await();
  }

  @Test
  public void testReadAsTextWithCharset() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        final StringBuilder body = new StringBuilder();
        http.onchunk(new Action<String>() {
          @Override
          public void on(String data) {
            body.append(data);
          }
        })
        .onend(new VoidAction() {
          @Override
          public void on() {
            threadAssertEquals(body.toString(), "시간 속에 만들어진 무대 위에 그대는 없다");
            resume();
          }
        })
        .readAsText("utf-8");
      }
    });
    client.newRequest(uri()).method(HttpMethod.POST)
    .content(new StringContentProvider("시간 속에 만들어진 무대 위에 그대는 없다", "utf-8"),
      "text/plain; charset=euc-kr").send(ASYNC);
    await();
  }

  @Test
  public void testReadBinary() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        http.onchunk(new Action<ByteBuffer>() {
          @Override
          public void on(ByteBuffer data) {
            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            body.write(bytes, 0, bytes.length);
          }
        })
        .onend(new VoidAction() {
          @Override
          public void on() {
            threadAssertTrue(Arrays.equals(body.toByteArray(), new byte[]{'h', 'i'}));
            resume();
          }
        })
        .read();
      }
    });
    client.newRequest(uri()).method(HttpMethod.POST)
    .content(new BytesContentProvider(new byte[]{'h', 'i'}), "application/octet-stream")
    .send(ASYNC);
    await();
  }

  @Test
  public void testReadAsBinary() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        http.onchunk(new Action<ByteBuffer>() {
          @Override
          public void on(ByteBuffer data) {
            byte[] bytes = new byte[data.remaining()];
            data.get(bytes);
            body.write(bytes, 0, bytes.length);
          }
        })
        .onend(new VoidAction() {
          @Override
          public void on() {
            threadAssertTrue(Arrays.equals(body.toByteArray(), new byte[]{'h', 'i'}));
            resume();
          }
        })
        .readAsBinary();
      }
    });
    client.newRequest(uri()).method(HttpMethod.POST)
    .content(new BytesContentProvider(new byte[]{'h', 'i'}), "text/plain")
    .send(ASYNC);
    await();
  }

  @Test
  public void testOnbodyWithText() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        http.onbody(new Action<String>() {
          @Override
          public void on(String data) {
            threadAssertEquals(data, "A Breath Clad In Happiness");
            resume();
          }
        })
        .read();
      }
    });
    client.newRequest(uri()).method(HttpMethod.POST)
    .content(new StringContentProvider("A Breath Clad In Happiness"), "text/plain; charset=utf-8")
    .send(ASYNC);
    await();
  }

  @Test
  public void testOnbodyWithBinary() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        http.onbody(new Action<ByteBuffer>() {
          @Override
          public void on(ByteBuffer data) {
            threadAssertEquals(data, ByteBuffer.wrap(new byte[]{'h', 'i'}));
            resume();
          }
        })
        .read();
      }
    });
    client.newRequest(uri()).method(HttpMethod.POST)
    .content(new BytesContentProvider(new byte[]{'h', 'i'}), "application/octet-stream")
    .send(ASYNC);
    await();
  }

  @Test
  public void testSetStatus() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        http.setStatus(HttpStatus.NOT_FOUND).end();
      }
    });
    client.newRequest(uri()).send(new Response.Listener.Adapter() {
      @Override
      public void onSuccess(Response response) {
        threadAssertEquals(response.getStatus(), 404);
        resume();
      }
    });
    await();
  }

  @Test
  public void testSetHeader() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        http.setHeader("A", "A").setHeader("B", Arrays.asList("B1", "B2")).end();
      }
    });
    client.newRequest(uri()).send(new Response.Listener.Adapter() {
      @Override
      public void onSuccess(Response res) {
        HttpFields headers = res.getHeaders();
        threadAssertTrue(headers.getFieldNamesCollection().containsAll(Arrays.asList("A", "B")));
        threadAssertEquals(headers.get("A"), "A");
        threadAssertEquals(headers.get("B"), "B1, B2");
        resume();
      }
    });
    await();
  }

  @Test
  public void testWriteText() throws Throwable {
    final CountDownLatch latch = new CountDownLatch(1);
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        http.setHeader("content-type", "text/plain; charset=euc-kr")
        .write("기억 속에 머무른 그 때의 모습으로 그때의 웃음으로")
        .end()
        .onfinish(new VoidAction() {
            @Override
            public void on() {
              latch.countDown();
            }
          });
      }
    });
    client.newRequest(uri()).send(new Response.Listener.Adapter() {
      String body;

      @Override
      public void onContent(Response response, ByteBuffer content) {
        body = Charset.forName("euc-kr").decode(content).toString();
      }

      @Override
      public void onSuccess(Response res) {
        try {
          latch.await();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        threadAssertEquals(body, "기억 속에 머무른 그 때의 모습으로 그때의 웃음으로");
        resume();
      }
    });
    await();
  }

  @Test
  public void testWriteTextWithCharset() throws Throwable {
    final CountDownLatch latch = new CountDownLatch(1);
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        http.onfinish(new VoidAction() {
          @Override
          public void on() {
            latch.countDown();
          }
        })
        .end("기억 속에 머무른 그 때의 모습으로 그때의 웃음으로", "euc-kr");
      }
    });
    client.newRequest(uri()).send(new Response.Listener.Adapter() {
      String body;

      @Override
      public void onContent(Response response, ByteBuffer content) {
        body = Charset.forName("euc-kr").decode(content).toString();
      }

      @Override
      public void onSuccess(Response res) {
        try {
          latch.await();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        threadAssertEquals(body, "기억 속에 머무른 그 때의 모습으로 그때의 웃음으로");
        resume();
      }
    });
    await();
  }

  @Test
  public void testWriteBinary() throws Throwable {
    final CountDownLatch latch = new CountDownLatch(1);
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        http.write(ByteBuffer.wrap(new byte[]{'h', 'e'}).asReadOnlyBuffer())
        .write(ByteBuffer.wrap(new byte[]{'l', 'l'}))
        .end(ByteBuffer.wrap(new byte[]{'o'}))
        .onfinish(new VoidAction() {
            @Override
            public void on() {
              latch.countDown();
            }
          });
      }
    });
    client.newRequest(uri()).send(new Response.Listener.Adapter() {
      ByteArrayOutputStream os = new ByteArrayOutputStream();

      @Override
      public void onContent(Response response, ByteBuffer content) {
        byte[] bytes = new byte[content.remaining()];
        content.get(bytes);
        os.write(bytes, 0, bytes.length);
      }

      @Override
      public void onSuccess(Response response) {
        try {
          latch.await();
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        threadAssertTrue(Arrays.equals(os.toByteArray(), new byte[]{'h', 'e', 'l', 'l', 'o'}));
        resume();
      }
    });
    await();
  }

  @Test
  public void testEnd() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        http.end();
      }
    });
    client.newRequest(uri()).send(new Response.Listener.Adapter() {
      @Override
      public void onSuccess(Response response) {
        resume();
      }
    });
    await();
  }

  @Test
  public void testOnclose() throws Throwable {
    requestAction(new Action<ServerHttpExchange>() {
      @Override
      public void on(ServerHttpExchange http) {
        http.onclose(new VoidAction() {
          @Override
          public void on() {
            resume();
          }
        });
      }
    });
    client.newRequest(uri())
    .listener(new Request.Listener.Adapter() {
      @Override
      public void onCommit(Request request) {
        request.abort(new Exception());
      }
    })
    .send(ASYNC);
    await();
  }

}
