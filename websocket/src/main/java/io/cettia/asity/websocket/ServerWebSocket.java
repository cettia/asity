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
package io.cettia.asity.websocket;

import io.cettia.asity.action.Action;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * Represents a server-side WebSocket.
 * <p/>
 * Implementations are not thread-safe.
 *
 * @author Donghwan Kim
 * @see <a href="http://tools.ietf.org/html/rfc6455">RFC6455 - The WebSocket
 * Protocol</a>
 */
public interface ServerWebSocket {

  /**
   * The URI used to connect.
   */
  String uri();

  /**
   * The names of the handshake request headers. HTTP header is not case-sensitive
   * but {@link Set} is case-sensitive.
   */
  Set<String> headerNames();

  /**
   * Returns the first handshake request header associated with the given name.
   */
  String header(String name);

  /**
   * Returns the handshake request headers associated with the given name or empty list
   * if no header is found.
   */
  List<String> headers(String name);

  /**
   * Closes the connection. This method has no side effect if called more than
   * once.
   */
  void close();

  /**
   * Sends a text frame through the connection.
   */
  ServerWebSocket send(String data);

  /**
   * Sends a binary frame through the connection.
   */
  ServerWebSocket send(ByteBuffer byteBuffer);

  /**
   * Attaches an action for the text frame.
   */
  ServerWebSocket ontext(Action<String> action);

  /**
   * Attaches an action for the binary frame.
   */
  ServerWebSocket onbinary(Action<ByteBuffer> action);

  /**
   * Attaches an action for the close event. After this event, the instance
   * shouldn't be used and all the other events will be disabled.
   */
  ServerWebSocket onclose(Action<Void> action);

  /**
   * Attaches an action to handle error from various things. Its exact
   * behavior is platform-specific and error created by the platform is
   * propagated.
   */
  ServerWebSocket onerror(Action<Throwable> action);

  /**
   * Returns the provider-specific component.
   */
  <T> T unwrap(Class<T> clazz);

}
