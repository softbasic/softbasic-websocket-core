package org.softbasic.websocket.core;


import org.softbasic.websocket.core.imp.WebSocketException;

/**
 * The error handler for the subscription.
 */
@FunctionalInterface
public interface SubscriptionErrorListener {
  void onError(WebSocketException exception);
}
