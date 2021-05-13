package org.softbasic.websocket.core;


/**
 * The close handler for the subscription.
 */
@FunctionalInterface
public interface SubscriptionCloseListener {
    void onClose();
}
