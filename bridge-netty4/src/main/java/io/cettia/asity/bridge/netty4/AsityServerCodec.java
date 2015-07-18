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
package io.cettia.asity.bridge.netty4;

import io.cettia.asity.action.Action;
import io.cettia.asity.action.Actions;
import io.cettia.asity.action.ConcurrentActions;
import io.cettia.asity.http.ServerHttpExchange;
import io.cettia.asity.websocket.ServerWebSocket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ChannelHandler to process {@link HttpRequest} and {@link HttpResponse} into
 * {@link NettyServerHttpExchange} and {@link NettyServerWebSocket}. When you
 * configure handler, you must add <strong>{@link HttpServerCodec}</strong> in
 * front of this handler.
 * <p>
 * 
 * <pre>
 * 
 * ChannelPipeline pipeline = ch.pipeline();
 * <strong>pipeline.addLast(new HttpServerCodec())</strong>
 * .addLast(new AsityServerCodec() {
 *     {@literal @}Override
 *     protected boolean accept(HttpRequest req) {
 *         return URI.create(req.getUri()).getPath().equals("/cettia");
 *     }
 * }
 * .onhttp(http -&gt {}).onwebsocket(ws -&gt {}));
 * </pre>
 *
 * @author Donghwan Kim
 */
public class AsityServerCodec extends ChannelInboundHandlerAdapter {

    private Actions<ServerHttpExchange> httpActions = new ConcurrentActions<>();
    private Actions<ServerWebSocket> wsActions = new ConcurrentActions<>();
    private Map<Channel, NettyServerHttpExchange> httpMap = new ConcurrentHashMap<>();
    private Map<Channel, NettyServerWebSocket> wsMap = new ConcurrentHashMap<>();
    private Map<Channel, FullHttpRequest> wsReqMap = new ConcurrentHashMap<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
            if (!accept(req)) {
                ctx.fireChannelRead(msg);
                return;
            }
            if (req.getMethod() == HttpMethod.GET && req.headers().contains(HttpHeaders.Names.UPGRADE, HttpHeaders.Values.WEBSOCKET, true)) {
                // Because WebSocketServerHandshaker requires FullHttpRequest
                FullHttpRequest wsRequest = new DefaultFullHttpRequest(req.getProtocolVersion(), req.getMethod(), req.getUri());
                wsRequest.headers().set(req.headers());
                wsReqMap.put(ctx.channel(), wsRequest);
                // Set timeout to avoid memory leak
                ctx.pipeline().addFirst(new ReadTimeoutHandler(5));
            } else {
                NettyServerHttpExchange http = new NettyServerHttpExchange(ctx, req);
                httpMap.put(ctx.channel(), http);
                httpActions.fire(http);
            }
        } else if (msg instanceof HttpContent) {
            FullHttpRequest wsReq = wsReqMap.get(ctx.channel());
            if (wsReq != null) {
                wsReq.content().writeBytes(((HttpContent) msg).content());
                if (msg instanceof LastHttpContent) {
                    wsReqMap.remove(ctx.channel());
                    // Cancel timeout
                    ctx.pipeline().remove(ReadTimeoutHandler.class);
                    WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(getWebSocketLocation(ctx.pipeline(), wsReq), null, true);
                    WebSocketServerHandshaker handshaker = factory.newHandshaker(wsReq);
                    if (handshaker == null) {
                        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                    } else {
                        handshaker.handshake(ctx.channel(), wsReq);
                        NettyServerWebSocket ws = new NettyServerWebSocket(ctx, wsReq, handshaker);
                        wsMap.put(ctx.channel(), ws);
                        wsActions.fire(ws);
                    }
                }
            } else {
                NettyServerHttpExchange http = httpMap.get(ctx.channel());
                if (http != null) {
                    http.handleChunk((HttpContent) msg);
                }
            }
        } else if (msg instanceof WebSocketFrame) {
            NettyServerWebSocket ws = wsMap.get(ctx.channel());
            if (ws != null) {
                ws.handleFrame((WebSocketFrame) msg);
            }
        }
    }
    
    /**
     * Whether to process this request or not. By default, it accepts every
     * request.
     */
    protected boolean accept(HttpRequest req) {
        return true;
    }

    private String getWebSocketLocation(ChannelPipeline pipeline, HttpRequest req) {
        return (pipeline.get(SslHandler.class) == null ? "ws://" : "wss://") + HttpHeaders.getHost(req) + req.getUri();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        NettyServerHttpExchange http = httpMap.get(ctx.channel());
        if (http != null) {
            http.handleError(cause);
        }
        NettyServerWebSocket ws = wsMap.get(ctx.channel());
        if (ws != null) {
            ws.handleError(cause);
        }
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        NettyServerHttpExchange http = httpMap.remove(ctx.channel());
        if (http != null) {
            http.handleClose();
        }
        NettyServerWebSocket ws = wsMap.remove(ctx.channel());
        if (ws != null) {
            ws.handleClose();
        }
        wsReqMap.remove(ctx.channel());
    }

    /**
     * Registers an action to be called when {@link ServerHttpExchange} is
     * available.
     */
    public AsityServerCodec onhttp(Action<ServerHttpExchange> action) {
        httpActions.add(action);
        return this;
    }

    /**
     * Registers an action to be called when {@link ServerWebSocket} is
     * available.
     */
    public AsityServerCodec onwebsocket(Action<ServerWebSocket> action) {
        wsActions.add(action);
        return this;
    }

}
