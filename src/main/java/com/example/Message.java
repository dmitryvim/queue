package com.example;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * queue based message
 */
public class Message {

    /**
     * message content
     */
    private final String body;

    /**
     * the uniq message parameter, to verify the message is requested to be deleted
     */
    private final String handler;

    public Message(@Nonnull String body, @Nonnull String handler) {
        this.body = body;
        this.handler = handler;
    }

    public Message(@Nonnull String body) {
        this(body, UUID.randomUUID().toString());
    }

    public String getHandler() {
        return handler;
    }

    public String getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (body != null ? !body.equals(message.body) : message.body != null) return false;
        return handler != null ? handler.equals(message.handler) : message.handler == null;
    }

    @Override
    public int hashCode() {
        int result = body != null ? body.hashCode() : 0;
        result = 31 * result + (handler != null ? handler.hashCode() : 0);
        return result;
    }
}
