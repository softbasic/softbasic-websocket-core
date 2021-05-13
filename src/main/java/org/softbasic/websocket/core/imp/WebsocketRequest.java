package org.softbasic.websocket.core.imp;


import lombok.Data;
import org.softbasic.websocket.core.*;


@Data
public class WebsocketRequest<T> {
    //请求名称
    private String name;

    /**
     * 只有正常回调构造
     * @param successCallback
     */
    public WebsocketRequest(SubscriptionListener<T> successCallback) {
        this.successCallback = successCallback;
    }

    /**
     * 正常回调和错误回调构造
     * @param successCallback
     * @param errorCallback
     */
    public WebsocketRequest(SubscriptionListener<T> successCallback, SubscriptionErrorListener errorCallback) {
        this.successCallback = successCallback;
        this.errorCallback = errorCallback;
    }

    /**
     * 正常回调、错误回调、关闭连接回调构造
     * @param successCallback
     * @param errorCallback
     * @param closeListener
     */
    public WebsocketRequest(SubscriptionListener<T> successCallback, SubscriptionErrorListener errorCallback, SubscriptionCloseListener closeListener) {
        this.successCallback = successCallback;
        this.errorCallback = errorCallback;
        this.closeListener = closeListener;

    }
    //用以连接成功后，发送请求
    SubscriptionHandler<WebSocketConnection> connectionHandler;
    //数据解析
    private SubscriptionResolver<T> dataResolver;
    //正常回调
    private SubscriptionListener<T> successCallback;
    //失败回调
    private SubscriptionErrorListener errorCallback;
    //连接关闭回调
    private SubscriptionCloseListener closeListener;


}
