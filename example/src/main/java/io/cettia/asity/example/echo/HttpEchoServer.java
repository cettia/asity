/*
 * Copyright 2019 the original author or authors.
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
import io.cettia.asity.http.HttpStatus;
import io.cettia.asity.http.ServerHttpExchange;

/**
 * @author Donghwan Kim
 */
public class HttpEchoServer implements Action<ServerHttpExchange> {
  @Override
  public void on(ServerHttpExchange http) {
    // Reads request URI, method and headers
    System.out.println(http.method() + " " + http.uri());
    http.headerNames().stream().forEach(name -> System.out.println(name + ": " + String.join(", ", http.headers(name))));

    // Writes response status code and headers
    http.setStatus(HttpStatus.OK).setHeader("content-type", http.header("content-type"));

    // Reads a chunk from request body and writes it to response body
    http.onchunk((String chunk) -> http.write(chunk)).readAsText();
    // If request body is binary,
    // http.onchunk((ByteBuffer binary) -> http.write(binary)).readAsBinary();

    // Ends response if request ends
    http.onend((Void v) -> http.end());

    // Exception handling
    http.onerror((Throwable t) -> t.printStackTrace()).onclose((Void v) -> System.out.println("disconnected"));
  }
}
