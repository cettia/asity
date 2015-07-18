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
package io.cettia.asity.test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import io.cettia.asity.action.Action;
import io.cettia.asity.action.VoidAction;
import io.cettia.asity.websocket.ServerWebSocket;

import java.net.ServerSocket;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

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

/**
 * Template class to test {@link ServerWebSocket}.
 *
 * @author Donghwan Kim
 */
public abstract class ServerWebSocketTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10);
    protected Performer performer;
    protected int port;

    @Before
    public void before() throws Exception {
        performer = new Performer();
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            port = serverSocket.getLocalPort();
        }
        startServer();
    }

    @After
    public void after() throws Exception {
        stopServer();
    }

    /**
     * Starts the server listening port {@link ServerWebSocketTest#port}
     * and if WebSocket's path is {@code /test}, create {@link ServerWebSocket}
     * and pass it to {@code performer.serverAction()}. This method is executed
     * following {@link Before}.
     */
    protected abstract void startServer() throws Exception;

    /**
     * Stops the server started in
     * {@link ServerWebSocketTest#startServer()}. This method is
     * executed following {@link After}.
     *
     * @throws Exception
     */
    protected abstract void stopServer() throws Exception;

    @Test
    public void uri() {
        performer.onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                assertThat(ws.uri(), is("/test?hello=there"));
                performer.start();
            }
        })
        .connect("/test?hello=there");
    }

    @Test
    public void close() {
        performer.clientListener(new WebSocketAdapter() {
            @Override
            public void onWebSocketClose(int statusCode, String reason) {
                performer.start();
            }
        })
        .onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                ws.close();
            }
        })
        .connect();
    }

    @Test
    public void close_idempotent() {
        performer.clientListener(new WebSocketAdapter() {
            @Override
            public void onWebSocketClose(int statusCode, String reason) {
                performer.start();
            }
        })
        .onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                ws.close();
                ws.close();
            }
        })
        .connect();
    }

    @Test
    public void send_text() {
        performer.clientListener(new WebSocketAdapter() {
            @Override
            public void onWebSocketText(String message) {
                assertThat(message, is("A Will Remains in the Ashes"));
                performer.start();
            }
        })
        .onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                ws.send("A Will Remains in the Ashes");
            }
        })
        .connect();
    }

    @Test
    public void send_binary() {
        performer.clientListener(new WebSocketAdapter() {
            @Override
            public void onWebSocketBinary(byte[] payload, int offset, int len) {
                assertThat(payload, is(new byte[] { 0x00, 0x01, 0x02 }));
                performer.start();
            }
        })
        .onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                ws.send(ByteBuffer.wrap(new byte[] { 0x00, 0x01, 0x02 }).asReadOnlyBuffer());
            }
        })
        .connect();
    }

    @Test
    public void send_text_and_binary() {
        performer.clientListener(new WebSocketAdapter() {
            boolean done;
            @Override
            public void onWebSocketText(String message) {
                assertThat(message, is("A Will Remains in the Ashes"));
                if (done) {
                    performer.start();
                } else {
                    done = true;
                }
            }
            @Override
            public void onWebSocketBinary(byte[] payload, int offset, int len) {
                assertThat(payload, is(new byte[] { 0x00, 0x01, 0x02 }));
                if (done) {
                    performer.start();
                } else {
                    done = true;
                }
            }
        })
        .onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                ws.send("A Will Remains in the Ashes").send(ByteBuffer.wrap(new byte[] { 0x00, 0x01, 0x02 }));
            }
        })
        .connect();
    }

    @Test
    public void ontext() {
        performer.clientListener(new WebSocketAdapter() {
            @Override
            public void onWebSocketConnect(Session sess) {
                sess.getRemote().sendString("A road of winds the water builds", new WriteCallback() {
                    @Override
                    public void writeSuccess() {
                        assertThat(true, is(true));
                    }

                    @Override
                    public void writeFailed(Throwable x) {
                        assertThat(true, is(false));
                    }
                });
            }
        })
        .onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                ws.ontext(new Action<String>() {
                    @Override
                    public void on(String data) {
                        assertThat(data, is("A road of winds the water builds"));
                        performer.start();
                    }
                });
            }
        })
        .connect();
    }

    @Test
    public void onbinary() {
        performer.clientListener(new WebSocketAdapter() {
            @Override
            public void onWebSocketConnect(Session sess) {
                sess.getRemote().sendBytes(ByteBuffer.wrap(new byte[] { 0x00, 0x01, 0x02 }), new WriteCallback() {
                    @Override
                    public void writeSuccess() {
                        assertThat(true, is(true));
                    }

                    @Override
                    public void writeFailed(Throwable x) {
                        assertThat(true, is(false));
                    }
                });
            }
        })
        .onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                ws.onbinary(new Action<ByteBuffer>() {
                    @Override
                    public void on(ByteBuffer data) {
                        assertThat(data, is(ByteBuffer.wrap(new byte[] { 0x00, 0x01, 0x02 })));
                        performer.start();
                    }
                });
            }
        })
        .connect();
    }

    @Test
    public void ontext_and_onbinary() {
        performer.clientListener(new WebSocketAdapter() {
            @Override
            public void onWebSocketConnect(Session sess) {
                sess.getRemote().sendString("A road of winds the water builds", new WriteCallback() {
                    @Override
                    public void writeSuccess() {
                        assertThat(true, is(true));
                    }

                    @Override
                    public void writeFailed(Throwable x) {
                        assertThat(true, is(false));
                    }
                });
                sess.getRemote().sendBytes(ByteBuffer.wrap(new byte[] { 0x00, 0x01, 0x02 }), new WriteCallback() {
                    @Override
                    public void writeSuccess() {
                        assertThat(true, is(true));
                    }

                    @Override
                    public void writeFailed(Throwable x) {
                        assertThat(true, is(false));
                    }
                });
            }
        })
        .onserver(new Action<ServerWebSocket>() {
            boolean done;
            @Override
            public void on(ServerWebSocket ws) {
                ws.ontext(new Action<String>() {
                    @Override
                    public void on(String data) {
                        assertThat(data, is("A road of winds the water builds"));
                        if (done) {
                            performer.start();
                        } else {
                            done = true;
                        }
                    }
                })
                .onbinary(new Action<ByteBuffer>() {
                    @Override
                    public void on(ByteBuffer data) {
                        assertThat(data, is(ByteBuffer.wrap(new byte[] { 0x00, 0x01, 0x02 })));
                        if (done) {
                            performer.start();
                        } else {
                            done = true;
                        }
                    }
                });
            }
        })
        .connect();
    }

    @Test
    public void onclose_by_server() {
        performer.onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(final ServerWebSocket ws) {
                ws.onclose(new VoidAction() {
                    @Override
                    public void on() {
                        performer.start();
                    }
                })
                .close();
            }
        })
        .connect();
    }

    @Test
    public void onclose_by_client() {
        performer.clientListener(new WebSocketAdapter() {
            @Override
            public void onWebSocketConnect(Session sess) {
                sess.close();
            }
        })
        .onserver(new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket ws) {
                ws.onclose(new VoidAction() {
                    @Override
                    public void on() {
                        performer.start();
                    }
                });
            }
        })
        .connect();
    }

    // TODO
    // Now errorAction depends on the underlying platform so that it's not easy
    // to test. However, with the consistent exception hierarchy, it might be
    // possible in the future.

    protected class Performer {

        CountDownLatch latch = new CountDownLatch(1);
        WebSocketListener clientListener = new WebSocketAdapter();
        Action<ServerWebSocket> serverAction = new Action<ServerWebSocket>() {
            @Override
            public void on(ServerWebSocket object) {
            }
        };

        public Performer clientListener(WebSocketListener clientListener) {
            this.clientListener = clientListener;
            return this;
        }

        public Action<ServerWebSocket> serverAction() {
            return new Action<ServerWebSocket>() {
                @Override
                public void on(ServerWebSocket ws) {
                    serverAction.on(ws);
                }
            };
        }

        public Performer onserver(Action<ServerWebSocket> serverAction) {
            this.serverAction = serverAction;
            return this;
        }

        public Performer connect() {
            return connect("/test");
        }

        public Performer connect(String uri) {
            WebSocketClient client = new WebSocketClient();
            try {
                client.start();
                client.connect(clientListener, URI.create("ws://localhost:" + port + uri));
                latch.await();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    client.stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return this;
        }

        public Performer start() {
            latch.countDown();
            return this;
        }

    }

}
