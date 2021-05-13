package org.softbasic.websocket.core.imp;

public enum WebSocketConnectionState {
    CREATED(0,"已创建"),
    CLOSED(1,"关闭的"),
    CLOSED_ON_ERROR(2,"出错关闭"),
    CONNECTED(3,"已连接");


    //状态码
    private int code;
    private String msg;


    WebSocketConnectionState(int code,String msg) {
        this.code = code;
        this.msg=msg;
    }
}
