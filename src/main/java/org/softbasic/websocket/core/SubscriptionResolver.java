package org.softbasic.websocket.core;

/**
 * 数据解析
 * @param <T>
 */
@FunctionalInterface
public interface SubscriptionResolver<T> {

  T parse(String response);
}
