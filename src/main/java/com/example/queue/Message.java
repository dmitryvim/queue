package com.example.queue;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * TODO write documentation
 */
public class Message {

    private final String body;

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
    public String toString() {
        return "Message{" +
                "body='" + body + '\'' +
                ", handler='" + handler + '\'' +
                '}';
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
