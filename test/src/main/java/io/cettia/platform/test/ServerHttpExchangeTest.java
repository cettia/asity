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
package io.cettia.platform.test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import io.cettia.platform.action.Action;
import io.cettia.platform.action.VoidAction;
import io.cettia.platform.http.HttpStatus;
import io.cettia.platform.http.ServerHttpExchange;

import java.io.ByteArrayOutputStream;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.BytesContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

/**
 * Template class to test {@link ServerHttpExchange}.
 *
 * @author Donghwan Kim
 */
public abstract class ServerHttpExchangeTest {

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
     * Starts the server listening port
     * {@link ServerHttpExchangeTest#port} and if HTTP request's path is
     * {@code /test}, create {@link ServerHttpExchange} and pass it to
     * {@code performer.serverAction()}. This method is executed following
     * {@link Before}.
     */
    protected abstract void startServer() throws Exception;

    /**
     * Stops the server started in
     * {@link ServerHttpExchangeTest#startServer()}. This method is
     * executed following {@link After}.
     *
     * @throws Exception
     */
    protected abstract void stopServer() throws Exception;

    @Test
    public void uri() {
        performer.onserver(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                assertThat(http.uri(), is("/test?hello=there"));
                performer.start();
            }
        })
        .send("/test?hello=there");
    }

    @Test
    public void method() {
        performer.onserver(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                assertThat(http.method(), is("POST"));
                performer.start();
            }
        })
        .requestListener(new Request.Listener.Adapter() {
            @Override
            public void onBegin(Request request) {
                request.method(HttpMethod.POST);
            }
        })
        .send();
    }

    @Test
    public void header() {
        performer.onserver(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                assertThat(http.headerNames(), either(hasItems("a", "b")).or(hasItems("A", "B")));
                assertThat(http.header("A"), is("A"));
                assertThat(http.header("B"), is("B1"));
                assertThat(http.headers("A"), contains("A"));
                assertThat(http.headers("B"), contains("B1", "B2"));
                performer.start();
            }
        })
        .requestListener(new Request.Listener.Adapter() {
            @Override
            public void onBegin(Request request) {
                request.header("A", "A").header("B", "B1").header("B", "B2");
            }
        })
        .send();
    }

    @Test
    public void read_text() {
        performer.onserver(new Action<ServerHttpExchange>() {
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
                        assertThat(body.toString(), is("A Breath Clad In Happiness"));
                        performer.start();
                    }
                })
                .read();
            }
        })
        .requestListener(new Request.Listener.Adapter() {
            @Override
            public void onBegin(Request request) {
                request.method(HttpMethod.POST).content(new StringContentProvider("A Breath Clad In Happiness"), "text/plain; charset=utf-8");
            }
        })
        .send();
    }

    @Test
    public void readAsText() {
        performer.onserver(new Action<ServerHttpExchange>() {
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
                        assertThat(body.toString(), is("Day 7: Poem of the Ocean"));
                        performer.start();
                    }
                })
                .readAsText();
            }
        })
        .requestListener(new Request.Listener.Adapter() {
            @Override
            public void onBegin(Request request) {
                request.method(HttpMethod.POST).content(new StringContentProvider("Day 7: Poem of the Ocean"), "application/octet-stream");
            }
        })
        .send();
    }

    @Test
    public void readAsText_charset() {
        performer.onserver(new Action<ServerHttpExchange>() {
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
                        assertThat(body.toString(), is("시간 속에 만들어진 무대 위에 그대는 없다"));
                        performer.start();
                    }
                })
                .readAsText("utf-8");
            }
        })
        .requestListener(new Request.Listener.Adapter() {
            @Override
            public void onBegin(Request request) {
                request.method(HttpMethod.POST).content(new StringContentProvider("시간 속에 만들어진 무대 위에 그대는 없다", "utf-8"), "text/plain; charset=euc-kr");
            }
        })
        .send();
    }

    @Test
    public void read_binary() {
        performer.onserver(new Action<ServerHttpExchange>() {
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
                        assertThat(body.toByteArray(), is(new byte[] { 'h', 'i' }));
                        performer.start();
                    }
                })
                .read();
            }
        })
        .requestListener(new Request.Listener.Adapter() {
            @Override
            public void onBegin(Request request) {
                request.method(HttpMethod.POST).content(new BytesContentProvider(new byte[] { 'h', 'i' }), "application/octet-stream");
            }
        })
        .send();
    }

    @Test
    public void readAsBinary() {
        performer.onserver(new Action<ServerHttpExchange>() {
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
                        assertThat(body.toByteArray(), is(new byte[] { 'h', 'i' }));
                        performer.start();
                    }
                })
                .readAsBinary();
            }
        })
        .requestListener(new Request.Listener.Adapter() {
            @Override
            public void onBegin(Request request) {
                request.method(HttpMethod.POST).content(new BytesContentProvider(new byte[] { 'h', 'i' }), "text/plain");
            }
        })
        .send();
    }

    @Test
    public void onbody_with_text() {
        performer.onserver(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                http.onbody(new Action<String>() {
                    @Override
                    public void on(String data) {
                        assertThat(data, is("A Breath Clad In Happiness"));
                        performer.start();
                    }
                })
                .read();
            }
        })
        .requestListener(new Request.Listener.Adapter() {
            @Override
            public void onBegin(Request request) {
                request.method(HttpMethod.POST).content(new StringContentProvider("A Breath Clad In Happiness"), "text/plain; charset=utf-8");
            }
        })
        .send();
    }

    @Test
    public void onbody_with_binary() {
        performer.onserver(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                http.onbody(new Action<ByteBuffer>() {
                    @Override
                    public void on(ByteBuffer data) {
                        assertThat(data, is(ByteBuffer.wrap(new byte[] { 'h', 'i' })));
                        performer.start();
                    }
                })
                .read();
            }
        })
        .requestListener(new Request.Listener.Adapter() {
            @Override
            public void onBegin(Request request) {
                request.method(HttpMethod.POST).content(new BytesContentProvider(new byte[] { 'h', 'i' }), "application/octet-stream");
            }
        })
        .send();
    }

    @Test
    public void setStatus() {
        performer.onserver(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                http.setStatus(HttpStatus.NOT_FOUND).end();
            }
        })
        .responseListener(new Response.Listener.Adapter() {
            @Override
            public void onSuccess(Response response) {
                assertThat(response.getStatus(), is(404));
                performer.start();
            }
        })
        .send();
    }

    @Test
    public void setHeader() {
        performer.onserver(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                http.setHeader("A", "A").setHeader("B", Arrays.asList("B1", "B2")).end();
            }
        })
        .responseListener(new Response.Listener.Adapter() {
            @Override
            public void onSuccess(Response res) {
                HttpFields headers = res.getHeaders();
                assertThat(headers.getFieldNamesCollection(), hasItems("A", "B"));
                assertThat(headers.get("A"), is("A"));
                assertThat(headers.get("B"), is("B1, B2"));
                performer.start();
            }
        })
        .send();
    }

    @Test
    public void write_text() {
        final CountDownLatch latch = new CountDownLatch(1);
        performer.onserver(new Action<ServerHttpExchange>() {
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
        })
        .responseListener(new Response.Listener.Adapter() {
            String body;

            @Override
            public void onContent(Response response, ByteBuffer content) {
                body = Charset.forName("euc-kr").decode(content).toString();
            }

            @Override
            public void onSuccess(Response response) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                assertThat(body, is("기억 속에 머무른 그 때의 모습으로 그때의 웃음으로"));
                performer.start();
            }
        })
        .send();
    }

    @Test
    public void write_text_charset() {
        final CountDownLatch latch = new CountDownLatch(1);
        performer.onserver(new Action<ServerHttpExchange>() {
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
        })
        .responseListener(new Response.Listener.Adapter() {
            String body;

            @Override
            public void onContent(Response response, ByteBuffer content) {
                body = Charset.forName("euc-kr").decode(content).toString();
            }

            @Override
            public void onSuccess(Response response) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                assertThat(body, is("기억 속에 머무른 그 때의 모습으로 그때의 웃음으로"));
                performer.start();
            }
        })
        .send();
    }

    @Test
    public void write_binary() {
        final CountDownLatch latch = new CountDownLatch(1);
        performer.onserver(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                http.write(ByteBuffer.wrap(new byte[] { 'h', 'e' }).asReadOnlyBuffer())
                .write(ByteBuffer.wrap(new byte[] { 'l', 'l' }))
                .end(ByteBuffer.wrap(new byte[] { 'o' }))
                .onfinish(new VoidAction() {
                    @Override
                    public void on() {
                        latch.countDown();
                    }
                });
            }
        })
        .responseListener(new Response.Listener.Adapter() {
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
                assertThat(os.toByteArray(), is(new byte[] { 'h', 'e', 'l', 'l', 'o' }));
                performer.start();
            }
        })
        .send();
    }

    @Test
    public void end() {
        performer.onserver(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                http.end();
            }
        })
        .responseListener(new Response.Listener.Adapter() {
            @Override
            public void onSuccess(Response response) {
                performer.start();
            }
        })
        .send();
    }

    @Test
    public void onclose() {
        performer.onserver(new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange http) {
                http.onclose(new VoidAction() {
                    @Override
                    public void on() {
                        performer.start();
                    }
                });
            }
        })
        .requestListener(new Request.Listener.Adapter() {
            @Override
            public void onCommit(Request request) {
                request.abort(new Exception());
            }
        })
        .send();
    }

    // TODO
    // Now errorAction depends on the underlying platform so that it's not easy
    // to test. However, with the consistent exception hierarchy, it might be
    // possible in the future.

    protected class Performer {

        CountDownLatch latch = new CountDownLatch(1);
        Request.Listener requestListener = new Request.Listener.Adapter();
        Response.Listener responseListener = new Response.Listener.Adapter();
        Action<ServerHttpExchange> serverAction = new Action<ServerHttpExchange>() {
            @Override
            public void on(ServerHttpExchange object) {
            }
        };

        public Performer requestListener(Request.Listener requestListener) {
            this.requestListener = requestListener;
            return this;
        }

        public Performer responseListener(Response.Listener responseListener) {
            this.responseListener = responseListener;
            return this;
        }

        public Action<ServerHttpExchange> serverAction() {
            return new Action<ServerHttpExchange>() {
                @Override
                public void on(ServerHttpExchange http) {
                    serverAction.on(http);
                }
            };
        }

        public Performer onserver(Action<ServerHttpExchange> serverAction) {
            this.serverAction = serverAction;
            return this;
        }

        public Performer send() {
            return send("/test");
        }

        public Performer send(String uri) {
            HttpClient client = new HttpClient();
            try {
                client.start();
                client.newRequest("http://localhost:" + port + uri).listener(requestListener).send(responseListener);
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
