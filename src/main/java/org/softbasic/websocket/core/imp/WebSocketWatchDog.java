package org.softbasic.websocket.core.imp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 监听器，主要是重连机制实现
 * 单例，恶汉模式
 */
public class WebSocketWatchDog {
    private static final Logger log = LoggerFactory.getLogger(WebSocketWatchDog.class);
    private static WebSocketWatchDog instance = new WebSocketWatchDog();

    public static WebSocketWatchDog getInstance() {
        return instance;
    }

    //存储所有连接
    private static final CopyOnWriteArrayList<WebSocketConnection> TIME_HELPER = new CopyOnWriteArrayList<>();


    //私有构造器，设置定时扫描
    private WebSocketWatchDog() {
        long t = 1_000;
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.scheduleAtFixedRate(() -> {
            TIME_HELPER.forEach(connection -> {
                if (!connection.isConnect()) {
                    connection.connect();
                }
            });
        }, t, t, TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(exec::shutdown));
    }

    void onConnectionCreated(WebSocketConnection connection) {
        TIME_HELPER.addIfAbsent(connection);
        log.info("WebSocketWatchDog add successfully,size:"+TIME_HELPER.size());
    }


    /**
     * 获取存活的连接
     * @return
     */
    public static List<Integer> getAliveConnetionId(){
        List<Integer> re=new ArrayList<>();
        TIME_HELPER.forEach(connection -> {
            if (connection.isConnect()) {
                re.add(connection.getConnectionId());
            }
        });
        return re;
    }
}
