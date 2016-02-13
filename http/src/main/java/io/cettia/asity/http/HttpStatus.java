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

/**
 * Represents the HTTP status code and reason phrase.
 *
 * @author Donghwan Kim
 * @see <a
 * href="http://www.iana.org/assignments/http-status-codes/http-status-codes.xhtml">HTTP
 * Status Code Registry</a>
 */
public class HttpStatus {

  // 1xx: Informational - Request received, continuing process
  /**
   * {@code 100 Continue}
   */
  public static final HttpStatus CONTINUE = new HttpStatus(100, "Continue");
  /**
   * {@code 101 Switching Protocols}
   */
  public static final HttpStatus SWITCHING_PROTOCOLS = new HttpStatus(101, "Switching Protocols");
  /**
   * {@code 102 Processing}
   */
  public static final HttpStatus PROCESSING = new HttpStatus(102, "Processing");

  // 2xx: Success - The action was successfully received, understood, and accepted
  /**
   * {@code 200 OK}
   */
  public static final HttpStatus OK = new HttpStatus(200, "OK");
  /**
   * {@code 201 Created}
   */
  public static final HttpStatus CREATED = new HttpStatus(201, "Created");
  /**
   * {@code 202 Accepted}
   */
  public static final HttpStatus ACCEPTED = new HttpStatus(202, "Accepted");
  /**
   * {@code 203 Non-Authoritative Information}
   */
  public static final HttpStatus NON_AUTHORITATIVE_INFORMATION = new HttpStatus(203,
    "Non-Authoritative Information");
  /**
   * {@code 204 No Content}
   */
  public static final HttpStatus NO_CONTENT = new HttpStatus(204, "No Content");
  /**
   * {@code 205 Reset Content}
   */
  public static final HttpStatus RESET_CONTENT = new HttpStatus(205, "Reset Content");
  /**
   * {@code 206 Partial Content}
   */
  public static final HttpStatus PARTIAL_CONTENT = new HttpStatus(206, "Partial Content");
  /**
   * {@code 207 Multi-Status}
   */
  public static final HttpStatus MULTI_STATUS = new HttpStatus(207, "Multi-Status");
  /**
   * {@code 208 Already Reported}
   */
  public static final HttpStatus ALREADY_REPORTED = new HttpStatus(208, "Already Reported");
  /**
   * {@code 226 IM Used}
   */
  public static final HttpStatus IM_USED = new HttpStatus(226, "IM Used");

  // 3xx: Redirection - Further action must be taken in order to complete the request
  /**
   * {@code 300 Multiple Choices}
   */
  public static final HttpStatus MULTIPLE_CHOICES = new HttpStatus(300, "Multiple Choices");
  /**
   * {@code 301 Moved Permanently}
   */
  public static final HttpStatus MOVED_PERMANENTLY = new HttpStatus(301, "Moved Permanently");
  /**
   * {@code 302 Found}
   */
  public static final HttpStatus FOUND = new HttpStatus(302, "Found");
  /**
   * {@code 303 See Other}
   */
  public static final HttpStatus SEE_OTHER = new HttpStatus(303, "See Other");
  /**
   * {@code 304 Not Modified}
   */
  public static final HttpStatus NOT_MODIFIED = new HttpStatus(304, "Not Modified");
  /**
   * {@code 305 Use Proxy}
   */
  public static final HttpStatus USE_PROXY = new HttpStatus(305, "Use Proxy");
  /**
   * {@code 306 Reserved}
   */
  public static final HttpStatus RESERVED = new HttpStatus(306, "Reserved");
  /**
   * {@code 307 Temporary Redirect}
   */
  public static final HttpStatus TEMPORARY_REDIRECT = new HttpStatus(307, "Temporary Redirect");
  /**
   * {@code 308 Permanent Redirect}
   */
  public static final HttpStatus PERMANENT_REDIRECT = new HttpStatus(308, "Permanent Redirect");

  // 4xx: Client Error - The request contains bad syntax or cannot be fulfilled
  /**
   * {@code 400 Bad Request}
   */
  public static final HttpStatus BAD_REQUEST = new HttpStatus(400, "Bad Request");
  /**
   * {@code 401 Unauthorized}
   */
  public static final HttpStatus UNAUTHORIZED = new HttpStatus(401, "Unauthorized");
  /**
   * {@code 402 Payment Required}
   */
  public static final HttpStatus PAYMENT_REQUIRED = new HttpStatus(402, "Payment Required");
  /**
   * {@code 403 Forbidden}
   */
  public static final HttpStatus FORBIDDEN = new HttpStatus(403, "Forbidden");
  /**
   * {@code 404 Not Found}
   */
  public static final HttpStatus NOT_FOUND = new HttpStatus(404, "Not Found");
  /**
   * {@code 405 Method Not Allowed}
   */
  public static final HttpStatus METHOD_NOT_ALLOWED = new HttpStatus(405, "Method Not Allowed");
  /**
   * {@code 406 Not Acceptable}
   */
  public static final HttpStatus NOT_ACCEPTABLE = new HttpStatus(406, "Not Acceptable");
  /**
   * {@code 407 Proxy Authentication Required}
   */
  public static final HttpStatus PROXY_AUTHENTICATION_REQUIRED = new HttpStatus(407, "Proxy " +
    "Authentication Required");
  /**
   * {@code 408 Request Timeout}
   */
  public static final HttpStatus REQUEST_TIMEOUT = new HttpStatus(408, "Request Timeout");
  /**
   * {@code 409 Conflict}
   */
  public static final HttpStatus CONFLICT = new HttpStatus(409, "Conflict");
  /**
   * {@code 410 Gone}
   */
  public static final HttpStatus GONE = new HttpStatus(410, "Gone");
  /**
   * {@code 411 Length Required}
   */
  public static final HttpStatus LENGTH_REQUIRED = new HttpStatus(411, "Length Required");
  /**
   * {@code 412 Precondition Failed}
   */
  public static final HttpStatus PRECONDITION_FAILED = new HttpStatus(412, "Precondition Failed");
  /**
   * {@code 413 Request Entity Too Large}
   */
  public static final HttpStatus REQUEST_ENTITY_TOO_LARGE = new HttpStatus(413, "Request Entity " +
    "Too Large");
  /**
   * {@code 414 Request-URI Too Long}
   */
  public static final HttpStatus REQUEST_URI_TOO_LONG = new HttpStatus(414, "Request-URI Too Long");
  /**
   * {@code 415 Unsupported Media Type}
   */
  public static final HttpStatus UNSUPPORTED_MEDIA_TYPE = new HttpStatus(415, "Unsupported Media " +
    "Type");
  /**
   * {@code 416 Requested Range Not Satisfiable}
   */
  public static final HttpStatus REQUESTED_RANGE_NOT_SATISFIABLE = new HttpStatus(416, "Requested" +
    " Range Not Satisfiable");
  /**
   * {@code 417 Expectation Failed}
   */
  public static final HttpStatus EXPECTATION_FAILED = new HttpStatus(417, "Expectation Failed");
  /**
   * {@code 422 Unprocessable Entity}
   */
  public static final HttpStatus UNPROCESSABLE_ENTITY = new HttpStatus(422, "Unprocessable Entity");
  /**
   * {@code 423 Locked}
   */
  public static final HttpStatus LOCKED = new HttpStatus(423, "Locked");
  /**
   * {@code 424 Failed Dependency}
   */
  public static final HttpStatus FAILED_DEPENDENCY = new HttpStatus(424, "Failed Dependency");
  /**
   * {@code 426 Upgrade Required}
   */
  public static final HttpStatus UPGRADE_REQUIRED = new HttpStatus(426, "Upgrade Required");
  /**
   * {@code 428 Precondition Required}
   */
  public static final HttpStatus PRECONDITION_REQUIRED = new HttpStatus(428, "Precondition " +
    "Required");
  /**
   * {@code 429 Too Many Requests}
   */
  public static final HttpStatus TOO_MANY_REQUESTS = new HttpStatus(429, "Too Many Requests");
  /**
   * {@code 431 Request Header Fields Too Large}
   */
  public static final HttpStatus REQUEST_HEADER_FIELDS_TOO_LARGE = new HttpStatus(431, "Request " +
    "Header Fields Too Large");

  // 5xx: Server Error - The server failed to fulfill an apparently valid request
  /**
   * {@code 500 Internal Server Error}
   */
  public static final HttpStatus INTERNAL_SERVER_ERROR = new HttpStatus(500, "Internal Server " +
    "Error");
  /**
   * {@code 501 Not Implemented}
   */
  public static final HttpStatus NOT_IMPLEMENTED = new HttpStatus(501, "Not Implemented");
  /**
   * {@code 502 Bad Gateway}
   */
  public static final HttpStatus BAD_GATEWAY = new HttpStatus(502, "Bad Gateway");
  /**
   * {@code 503 Service Unavailable}
   */
  public static final HttpStatus SERVICE_UNAVAILABLE = new HttpStatus(503, "Service Unavailable");
  /**
   * {@code 504 Gateway Timeout}
   */
  public static final HttpStatus GATEWAY_TIMEOUT = new HttpStatus(504, "Gateway Timeout");
  /**
   * {@code 505 HTTP Version Not Supported}
   */
  public static final HttpStatus HTTP_VERSION_NOT_SUPPORTED = new HttpStatus(505, "HTTP Version " +
    "Not Supported");
  /**
   * {@code 506 Variant Also Negotiates (Experimental)}
   */
  public static final HttpStatus VARIANT_ALSO_NEGOTIATES = new HttpStatus(506, "Variant Also " +
    "Negotiates (Experimental)");
  /**
   * {@code 507 Insufficient Storage}
   */
  public static final HttpStatus INSUFFICIENT_STORAGE = new HttpStatus(507, "Insufficient Storage");
  /**
   * {@code 508 Loop Detected}
   */
  public static final HttpStatus LOOP_DETECTED = new HttpStatus(508, "Loop Detected");
  /**
   * {@code 510 Not Extended}
   */
  public static final HttpStatus NOT_EXTENDED = new HttpStatus(510, "Not Extended");
  /**
   * {@code 511 Network Authentication Required}
   */
  public static final HttpStatus NETWORK_AUTHENTICATION_REQUIRED = new HttpStatus(511, "Network " +
    "Authentication Required");

  private int code;
  private String reason;

  /**
   * Creates a status with the given status code.
   */
  public HttpStatus(int code) {
    this(code, null);
  }

  /**
   * Creates a status with the given status code and reason.
   */
  public HttpStatus(int code, String reason) {
    this.code = code;
    this.reason = reason;
  }

  /**
   * Returns the status code.
   */
  public int code() {
    return code;
  }

  /**
   * Returns the reason phrase.
   */
  public String reason() {
    return reason;
  }

  /**
   * Creates a status with new reason.
   */
  public HttpStatus newReason(String reason) {
    return new HttpStatus(code, reason);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + code;
    result = prime * result + ((reason == null) ? 0 : reason.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    HttpStatus other = (HttpStatus) obj;
    if (code != other.code) {
      return false;
    }
    if (reason == null) {
      if (other.reason != null) {
        return false;
      }
    } else if (!reason.equals(other.reason)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "HttpStatusCode [code=" + code + ", reason=" + reason + "]";
  }

}
