package org.softbasic.websocket.core.imp;


import okhttp3.*;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.softbasic.websocket.core.SubscriptionProtocolFilter;

/**
 * 封装WebSocket连接，增强底层websocket网络通信功能，增加状态、回调、重连等机制
 * <p>
 * 总体设计为：
 * ①、一个WebSocket处理一个类型的请求，在底层websocket创建连接服务器以后，通过connectionHandler回调做请求动作
 * ②、该WebSocket不再发送其他类型的请求，其onMessage始终接收同一个请求的数据，由相应特定的dataResolver来解析数据，并由对应的successCallback做成功回调
 * <p>
 * 所以，每一个WebSocket的功能，包括其请求参数、数据接收时解析、数据解析成功回调、异常回调、权限验证回调等动作，都由创建时的WebsocketRequest定义好的
 * <p>
 * 对连接存活高度依赖的业务，可通过closeListener回调来达到连接断开时实时性的控制
 */
public class WebSocketConnection extends WebSocketListener {
    private static final Logger log = LoggerFactory.getLogger(WebSocketConnection.class);
    //连接计数器
    private static int connectionCounter;



    //当前连接标识，默认从0开始
    private int connectionId;
    //OkHttp-客户端
    private static final OkHttpClient client = new OkHttpClient();
    //OkHttp-请求
    private final Request okhttpRequest;
    //实际承担网络请求的TCP通道Websocket
    private WebSocket webSocket;
    //连接状态,默认是关闭的
    private volatile WebSocketConnectionState state;
    //订阅请求
    private final WebsocketRequest request;
    //SOCKET监听器
    private final WebSocketWatchDog watchDog;
    //密钥
    private final String apiKey, secretKey;
    //
    private final SubscriptionProtocolFilter protocolFilter;

    /**
     * 构造器
     *
     * @param apiKey
     * @param secretKey
     * @param options        订阅选项，主要是订阅的服务器地址
     * @param request        订阅请求，主要是订阅的频道地址及回调函数
     * @param protocolFilter 协议过滤器，主要是处理数据解压、ping pong等基础服务
     */
    public WebSocketConnection(String apiKey, String secretKey, SubscriptionOptions options, WebsocketRequest request, SubscriptionProtocolFilter protocolFilter) {
        this.connectionId = connectionCounter++;
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.request = request;
        this.okhttpRequest = new Request.Builder().url(options.getUri()).build();
        this.watchDog = WebSocketWatchDog.getInstance();
        this.protocolFilter=protocolFilter;
        state = WebSocketConnectionState.CREATED;
        log.info("[Sub] Connection [id: " + this.connectionId + "] created for " + request.getName());
    }

    /**
     * 连接服务器
     */
    public void connect() {
        if (state == WebSocketConnectionState.CONNECTED) {
            log.info("[Sub][" + this.connectionId + "] Already connected");
            return;
        }
        log.info("[Sub][" + this.connectionId + "] Connecting...");
        webSocket = client.newWebSocket(okhttpRequest, this);
    }

    /**
     * 发送请求
     *
     * @param message
     */
    public void send(String message) {
        boolean result = false;
        log.info("[Send]{}", message);
        if (webSocket != null) {
            result = webSocket.send(message);
        }
        if (!result) {
            log.error("[Sub][" + this.connectionId + "] Failed to send message");
            closeOnError();
        }
    }

    /**
     * 连接打开，可以开始传输数据了
     *
     * @param webSocket
     * @param response
     */
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        //保存当前socket通信
        this.webSocket = webSocket;
        //加入监控器
        watchDog.onConnectionCreated(this);
        if (request.connectionHandler != null) {
            request.connectionHandler.handle(this);
        }
        state = WebSocketConnectionState.CONNECTED;
        log.info("[Sub][" + this.connectionId + "] Connected to server");
    }


    /**
     * 错误
     *
     * @param webSocket
     * @param t
     * @param response
     */
    @Override
    public void onFailure(final WebSocket webSocket, final Throwable t, final Response response) {
        state = WebSocketConnectionState.CLOSED;
        //异常回调
        dataError("onFailure", t);
        //断开连接
        closeOnError();
    }

    /**
     * 连接关闭回调
     *
     * @param webSocket
     * @param code
     * @param reason
     */
    @Override
    public void onClosed(final WebSocket webSocket, final int code, final String reason) {
        state = WebSocketConnectionState.CLOSED;
        //连接关闭回调
        if (request.getCloseListener() != null) {
            request.getCloseListener().onClose();
        }
    }

    /**
     * 接收数据
     *
     * @param webSocket
     * @param bytes
     */
    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        try {
            protocolFilter.handle(this,request,bytes);
        } catch (Exception e) {
            dataError("Failed to parse server's response: " + e.getMessage(), e);
        }
    }

    /**
     * 是否正常连接中
     *
     * @return
     */
    public boolean isConnect() {
        return state == WebSocketConnectionState.CONNECTED ? true : false;
    }

    //数据运行时错误，调用异常回调
    private void dataError(String errorMessage, Throwable t) {
        //失败回调
        if (request.getErrorCallback() != null) {
            WebSocketException exception = new WebSocketException(WebSocketException.SUBSCRIPTION_ERROR, t.getMessage(), t);
            request.getErrorCallback().onError(exception);
        }
        log.error("[Sub][" + this.connectionId + "] " + errorMessage);
    }

    /**
     * 其他严重错误，关闭SOCKET
     */
    private void closeOnError() {
        if (webSocket != null) {
            this.webSocket.cancel();
            state = WebSocketConnectionState.CLOSED_ON_ERROR;
            log.error("[Sub][" + this.connectionId + "] Connection is closing cause by error");
        }
    }
    public int getConnectionId() {
        return connectionId;
    }
}
