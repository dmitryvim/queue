package com.example;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * TODO write documentation
 */
public class Message {
    private final LocalDateTime timestamp;

    private final String message;

    //TODO equals for message delete

    public Message(@Nonnull LocalDateTime timestamp, @Nonnull String message) {
        //TODO check for null
        this.timestamp = timestamp;
        this.message = message;
    }

    public Message(@Nonnull String message) {
        this(LocalDateTime.now(), message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message1 = (Message) o;

        return (timestamp != null ? timestamp.equals(message1.timestamp) : message1.timestamp == null) && (message != null ? message.equals(message1.message) : message1.message == null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, message);
    }
}
