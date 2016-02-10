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
import io.cettia.asity.websocket.ServerWebSocket;
import net.jodah.concurrentunit.ConcurrentTestCase;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.net.ServerSocket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Donghwan Kim
 */
public abstract class ServerWebSocketTestBase extends ConcurrentTestCase {

  protected static final String TEST_URI = "/test";
  private static final WebSocketListener NOOP = new WebSocketAdapter();
  private static final WriteCallback ASYNC = new WriteCallback() {
    @Override
    public void writeSuccess() {
    }

    @Override
    public void writeFailed(Throwable throwable) {
    }
  };

  @Rule
  public Timeout globalTimeout = Timeout.seconds(30);
  protected WebSocketClient client = new WebSocketClient();

  private int port;
  private Action<ServerWebSocket> websocketAction;

  @Before
  public void setUp() throws Exception {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      port = serverSocket.getLocalPort();
    }
    client.start();
    startServer(port, new Action<ServerWebSocket>() {
      @Override
      public void on(ServerWebSocket ws) {
        websocketAction.on(ws);
      }
    });
  }

  @After
  public void tearDown() throws Exception {
    stopServer();
    client.stop();
    websocketAction = null;
  }

  protected abstract void startServer(int port, Action<ServerWebSocket> websocketAction) throws Exception;

  protected abstract void stopServer() throws Exception;

  protected String uri() {
    return uri(TEST_URI);
  }

  protected String uri(String path) {
    return "ws://localhost:" + port + path;
  }

  protected void websocketAction(Action<ServerWebSocket> websocketAction) {
    this.websocketAction = websocketAction;
  }

  @Test
  public void testURI() throws Throwable {
    websocketAction(new Action<ServerWebSocket>() {
      @Override
      public void on(ServerWebSocket ws) {
        threadAssertEquals(ws.uri(), "/test?hello=there");
        resume();
      }
    });
    client.connect(NOOP, URI.create(uri("/test?hello=there")));
    await();
  }

  @Test
  public void testClose() throws Throwable {
    websocketAction(new Action<ServerWebSocket>() {
      @Override
      public void on(ServerWebSocket ws) {
        ws.close();
      }
    });
    client.connect(new WebSocketAdapter() {
      @Override
      public void onWebSocketClose(int statusCode, String reason) {
        resume();
      }
    }, URI.create(uri()));
    await();
  }

  @Test
  public void testSendTextFrame() throws Throwable {
    websocketAction(new Action<ServerWebSocket>() {
      @Override
      public void on(ServerWebSocket ws) {
        ws.send("A Will Remains in the Ashes");
      }
    });
    client.connect(new WebSocketAdapter() {
      @Override
      public void onWebSocketText(String message) {
        threadAssertEquals(message, "A Will Remains in the Ashes");
        resume();
      }
    }, URI.create(uri()));
    await();
  }

  @Test
  public void testSendBinaryFrame() throws Throwable {
    websocketAction(new Action<ServerWebSocket>() {
      @Override
      public void on(ServerWebSocket ws) {
        ws.send(ByteBuffer.wrap(new byte[]{0x00, 0x01, 0x02}).asReadOnlyBuffer());
      }
    });
    client.connect(new WebSocketAdapter() {
      @Override
      public void onWebSocketBinary(byte[] payload, int offset, int len) {
        threadAssertTrue(Arrays.equals(payload, new byte[]{0x00, 0x01, 0x02}));
        resume();
      }
    }, URI.create(uri()));
    await();
  }

  @Test
  public void testSendTextFrameAndBinaryFrameTogether() throws Throwable {
    websocketAction(new Action<ServerWebSocket>() {
      @Override
      public void on(ServerWebSocket ws) {
        ws.send("A Will Remains in the Ashes").send(ByteBuffer.wrap(new byte[]{0x00, 0x01, 0x02}));
      }
    });
    client.connect(new WebSocketAdapter() {
      boolean done;

      @Override
      public void onWebSocketText(String message) {
        threadAssertEquals(message, "A Will Remains in the Ashes");
        if (done) {
          resume();
        } else {
          done = true;
        }
      }

      @Override
      public void onWebSocketBinary(byte[] payload, int offset, int len) {
        threadAssertTrue(Arrays.equals(payload, new byte[]{0x00, 0x01, 0x02}));
        if (done) {
          resume();
        } else {
          done = true;
        }
      }
    }, URI.create(uri()));
    await();
  }

  @Test
  public void testOntext() throws Throwable {
    websocketAction(new Action<ServerWebSocket>() {
      @Override
      public void on(ServerWebSocket ws) {
        ws.ontext(new Action<String>() {
          @Override
          public void on(String data) {
            threadAssertEquals(data, "A road of winds the water builds");
            resume();
          }
        });
      }
    });
    client.connect(new WebSocketAdapter() {
      @Override
      public void onWebSocketConnect(Session session) {
        session.getRemote().sendString("A road of winds the water builds", ASYNC);
      }
    }, URI.create(uri()));
    await();
  }

  @Test
  public void testOnbinary() throws Throwable {
    websocketAction(new Action<ServerWebSocket>() {
      @Override
      public void on(ServerWebSocket ws) {
        ws.onbinary(new Action<ByteBuffer>() {
          @Override
          public void on(ByteBuffer data) {
            threadAssertEquals(data, ByteBuffer.wrap(new byte[]{0x00, 0x01, 0x02}));
            resume();
          }
        });
      }
    });
    client.connect(new WebSocketAdapter() {
      @Override
      public void onWebSocketConnect(Session session) {
        session.getRemote().sendBytes(ByteBuffer.wrap(new byte[]{0x00, 0x01, 0x02}), ASYNC);
      }
    }, URI.create(uri()));
    await();
  }

  @Test
  public void testOntextAndOnbinary() throws Throwable {
    websocketAction(new Action<ServerWebSocket>() {
      boolean done;

      @Override
      public void on(ServerWebSocket ws) {
        ws.ontext(new Action<String>() {
          @Override
          public void on(String data) {
            threadAssertEquals(data, "A road of winds the water builds");
            if (done) {
              resume();
            } else {
              done = true;
            }
          }
        })
          .onbinary(new Action<ByteBuffer>() {
            @Override
            public void on(ByteBuffer data) {
              threadAssertEquals(data, ByteBuffer.wrap(new byte[]{0x00, 0x01, 0x02}));
              if (done) {
                resume();
              } else {
                done = true;
              }
            }
          });
      }
    });
    client.connect(new WebSocketAdapter() {
      @Override
      public void onWebSocketConnect(Session session) {
        session.getRemote().sendString("A road of winds the water builds", ASYNC);
        session.getRemote().sendBytes(ByteBuffer.wrap(new byte[]{0x00, 0x01, 0x02}), ASYNC);
      }
    }, URI.create(uri()));
    await();
  }

  @Test
  public void testOncloseByServer() throws Throwable {
    websocketAction(new Action<ServerWebSocket>() {
      @Override
      public void on(ServerWebSocket ws) {
        ws.onclose(new VoidAction() {
          @Override
          public void on() {
            resume();
          }
        })
          .close();
      }
    });
    client.connect(NOOP, URI.create(uri()));
    await();
  }

  @Test
  public void testOncloseByClient() throws Throwable {
    websocketAction(new Action<ServerWebSocket>() {
      @Override
      public void on(ServerWebSocket ws) {
        ws.onclose(new VoidAction() {
          @Override
          public void on() {
            resume();
          }
        });
      }
    });
    client.connect(new WebSocketAdapter() {
      @Override
      public void onWebSocketConnect(Session session) {
        session.close();
      }
    }, URI.create(uri()));
    await();
  }

}
