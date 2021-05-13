package org.softbasic.websocket.core.imp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * The configuration for the subscription APIs
 */
public class SubscriptionOptions {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionOptions.class);
    private String uri ;

    public SubscriptionOptions() { }
    public SubscriptionOptions(SubscriptionOptions options) {
        this.uri = options.uri;
    }



    /**
     * Set the URI for subscription.
     *
     * @param uri The URI name like "wss://api.huobi.pro".
     */
    public void setUri(String uri) {
        try {
            URI u = new URI(uri);
        } catch (Exception e) {
            throw new WebSocketException(WebSocketException.INPUT_ERROR, "The URI is incorrect: " + e.getMessage());
        }
        this.uri = uri;
    }


    public String getUri() {
        return uri;
    }
}
