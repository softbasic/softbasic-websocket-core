package org.softbasic.websocket.core;

@FunctionalInterface
public interface SubscriptionHandler<T> {
  void handle(T t);
}
