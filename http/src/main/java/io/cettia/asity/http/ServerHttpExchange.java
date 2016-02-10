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
package io.cettia.asity.http;

import io.cettia.asity.action.Action;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * Represents a server-side HTTP request-response exchange.
 * <p/>
 * Implementations are not thread-safe.
 *
 * @author Donghwan Kim
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616.html">RFC2616 -
 * Hypertext Transfer Protocol -- HTTP/1.1</a>
 */
public interface ServerHttpExchange {

  /**
   * The request URI.
   */
  String uri();

  /**
   * The name of the request method.
   */
  HttpMethod method();

  /**
   * The names of the request headers. HTTP header is not case-sensitive but
   * {@link Set} is case-sensitive.
   */
  Set<String> headerNames();

  /**
   * Returns the first request header associated with the given name.
   */
  String header(String name);

  /**
   * Returns the request headers associated with the given name or empty list
   * if no header is found.
   */
  List<String> headers(String name);

  /**
   * Reads the request body. If the request header, {@code content-type},
   * starts with {@code text/}, the body is read as text, and if not, as
   * binary. In case of text body, the charset is also determined by the same
   * header. If it's not given, {@code ISO-8859-1} is used by default.
   * <p/>
   * The read data will be passed to event handlers as {@link String} if it's
   * text and {@link ByteBuffer} if it's binary attached through
   * {@link ServerHttpExchange#onchunk(Action)}.
   * <p/>
   * This method should be called after adding
   * {@link ServerHttpExchange#onchunk(Action)},
   * {@link ServerHttpExchange#onbody(Action)}, and
   * {@link ServerHttpExchange#onend(Action)} and has no side effect if called
   * more than once.
   */
  ServerHttpExchange read();

  /**
   * Reads the request body as text. The charset is determined by the request
   * header, {@code content-type}. If it's not given, {@code ISO-8859-1} is
   * used by default.
   * <p/>
   * The read data will be passed to event handlers as {@link String} attached
   * through {@link ServerHttpExchange#onchunk(Action)}.
   * <p/>
   * This method should be called after adding
   * {@link ServerHttpExchange#onchunk(Action)},
   * {@link ServerHttpExchange#onbody(Action)}, and
   * {@link ServerHttpExchange#onend(Action)} and has no side effect if called
   * more than once.
   */
  ServerHttpExchange readAsText();

  /**
   * Reads the request body as text using the given charset.
   * <p/>
   * The read data will be passed to event handlers as {@link String} attached
   * through {@link ServerHttpExchange#onchunk(Action)}.
   * <p/>
   * This method should be called after adding
   * {@link ServerHttpExchange#onchunk(Action)},
   * {@link ServerHttpExchange#onbody(Action)}, and
   * {@link ServerHttpExchange#onend(Action)} and has no side effect if called
   * more than once.
   */
  ServerHttpExchange readAsText(String charsetName);

  /**
   * Reads the request body as binary.
   * <p/>
   * The read data will be passed to event handlers as {@link ByteBuffer}
   * attached through {@link ServerHttpExchange#onchunk(Action)}.
   * <p/>
   * This method should be called after adding
   * {@link ServerHttpExchange#onchunk(Action)},
   * {@link ServerHttpExchange#onbody(Action)}, and
   * {@link ServerHttpExchange#onend(Action)} and has no side effect if called
   * more than once.
   */
  ServerHttpExchange readAsBinary();

  /**
   * Attaches an action to be called with a chunk from the request body. The
   * allowed data type is {@link String} for text body and {@link ByteBuffer}
   * for binary body.
   */
  ServerHttpExchange onchunk(Action<?> action);

  /**
   * Attaches an action to be called when the request is fully read. It's the
   * end of the request.
   */
  ServerHttpExchange onend(Action<Void> action);

  /**
   * Attaches an action to be called with the whole request body. The allowed
   * data type is {@link String} for text body and {@link ByteBuffer} for
   * binary body. If the body is quite big, it may drain memory quickly. If
   * that's the case, use {@link ServerHttpExchange#onchunk(Action)} and
   * {@link ServerHttpExchange#onend(Action)}.
   */
  ServerHttpExchange onbody(Action<?> action);

  /**
   * Sets the HTTP status for the response.
   */
  ServerHttpExchange setStatus(HttpStatus status);

  /**
   * Sets a response header.
   */
  ServerHttpExchange setHeader(String name, String value);

  /**
   * Sets response headers.
   */
  ServerHttpExchange setHeader(String name, Iterable<String> value);

  /**
   * Writes a text chunk to the response body using the charset from the
   * response header, {@code content-type}. If it's not given,
   * {@code ISO-8859-1} is used.
   */
  ServerHttpExchange write(String data);

  /**
   * Writes a text chunk to the response body using the given charset.
   */
  ServerHttpExchange write(String data, String charsetName);

  /**
   * Writes a binary chunk to the response body.
   */
  ServerHttpExchange write(ByteBuffer byteBuffer);

  /**
   * Completes the response. Each exchange's response must be finished with
   * this method when done. It's the end of the response. This method has no
   * side effect if called more than once.
   */
  ServerHttpExchange end();

  /**
   * Writes a text chunk to the response body using the charset from the
   * response header, {@code content-type} and completes the response through
   * {@link ServerHttpExchange#end()}.
   */
  ServerHttpExchange end(String data);

  /**
   * Writes a text chunk to the response body using the given charset and
   * completes the response through {@link ServerHttpExchange#end()}.
   */
  ServerHttpExchange end(String data, String charsetName);

  /**
   * Writes a binary chunk to the response body and completes the response
   * through {@link ServerHttpExchange#end()}.
   */
  ServerHttpExchange end(ByteBuffer byteBuffer);

  /**
   * Attaches an action to be called when the response is fully written. It's
   * the end of the response.
   */
  ServerHttpExchange onfinish(Action<Void> action);

  /**
   * Attaches an action to be called when this exchange gets an error. It may
   * or may not accompany the closure of connection. Its exact behavior is
   * platform-specific and error created by the platform is propagated.
   */
  ServerHttpExchange onerror(Action<Throwable> action);

  /**
   * Attaches an action when the underlying connection is aborted for some
   * reason like an error. After this event, all the other event will be
   * disabled.
   */
  ServerHttpExchange onclose(Action<Void> action);

  /**
   * Returns the provider-specific component.
   */
  <T> T unwrap(Class<T> clazz);

}
