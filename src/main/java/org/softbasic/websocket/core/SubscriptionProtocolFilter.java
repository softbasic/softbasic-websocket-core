package org.softbasic.websocket.core;

import okio.ByteString;
import org.softbasic.websocket.core.imp.WebSocketConnection;
import org.softbasic.websocket.core.imp.WebsocketRequest;

/**
 * 协议过滤器
 * 负责数据解压、ping pong请求等基础共同服务
 */
public interface SubscriptionProtocolFilter {
    void handle(WebSocketConnection webSocketConnection, WebsocketRequest request, ByteString response);
}
