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
package io.cettia.platform.bridge.play2;

import io.cettia.platform.action.Action;
import io.cettia.platform.http.AbstractServerHttpExchange;
import io.cettia.platform.http.HttpStatus;
import io.cettia.platform.http.ServerHttpExchange;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import play.libs.F.Callback0;
import play.libs.F.Function0;
import play.libs.F.Promise;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Results.ByteChunks;
import play.mvc.Results.Chunks;

/**
 * {@link ServerHttpExchange} for Play 2.
 *
 * @author Donghwan Kim
 */
public class PlayServerHttpExchange extends AbstractServerHttpExchange {

    private final Request request;
    private final Response response;
    private boolean aborted;
    private CountDownLatch written = new CountDownLatch(1);
    private List<byte[]> buffer = new ArrayList<>();
    private HttpStatus status = HttpStatus.OK;
    private Chunks.Out<byte[]> out;

    public PlayServerHttpExchange(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    public Promise<Result> result() {
        return Promise.promise(new Function0<Result>() {
            @Override
            public Result apply() throws Throwable {
                // Block the current thread until the first call of write or close
                written.await();
                // Because ServerHttpExchange is not thread-safe
                synchronized (PlayServerHttpExchange.this) {
                    return Results.status(status.code(), new ByteChunks() {
                        @Override
                        public void onReady(Chunks.Out<byte[]> out) {
                            // With the same reason as above
                            synchronized (PlayServerHttpExchange.this) {
                                PlayServerHttpExchange.this.out = out;
                                out.onDisconnected(new Callback0() {
                                    @Override
                                    public void invoke() throws Throwable {
                                        closeActions.fire();
                                    }
                                });
                                for (byte[] data : buffer) {
                                    out.write(data);
                                }
                                if (aborted) {
                                    out.close();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public String uri() {
        return request.uri();
    }

    @Override
    public String method() {
        return request.method();
    }

    @Override
    public Set<String> headerNames() {
        return request.headers().keySet();
    }

    @Override
    public List<String> headers(String name) {
        for (String h : request.headers().keySet()) {
            if (name.toLowerCase().equals(h.toLowerCase())) {
                return Arrays.asList(request.headers().get(h));
            }
        }
        return Collections.<String> emptyList();
    }

    // Play can't read body asynchronously
    @Override
    protected void doRead(final Action<ByteBuffer> chunkAction) {
        // Using one of Play's thread pools may be better?
        new Thread(new Runnable() {
            @Override
            public void run() {
                chunkAction.on(ByteBuffer.wrap(request.body().asRaw().asBytes()));
                endActions.fire();
            }
        })
        .start();
    }
    
    private void throwIfWritten() {
        if (written.getCount() == 0) {
            errorActions.fire(new IllegalStateException("Response has already been written"));
        }
    }

    @Override
    protected void doSetStatus(HttpStatus status) {
        throwIfWritten();
        this.status = status;
    }

    @Override
    protected void doSetHeader(String name, String value) {
        throwIfWritten();
        // https://github.com/playframework/playframework/issues/2726
        if (name.equalsIgnoreCase(Response.CONTENT_TYPE)) {
            name = Response.CONTENT_TYPE;
        }
        response.setHeader(name, value);
    }

    @Override
    protected void doWrite(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        if (out == null) {
            written.countDown();
            buffer.add(bytes);
        } else {
            out.write(bytes);
        }
    }

    @Override
    protected void doEnd() {
        if (out == null) {
            written.countDown();
            aborted = true;
        } else {
            out.close();
        }
    }

    /**
     * {@link Request} and {@link Response} are available.
     */
    @Override
    public <T> T unwrap(Class<T> clazz) {
        return Request.class.isAssignableFrom(clazz) ?
                clazz.cast(request) :
                Response.class.isAssignableFrom(clazz) ?
                        clazz.cast(response) :
                        null;
    }

}
