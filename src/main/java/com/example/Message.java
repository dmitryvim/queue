package com.example;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;

/**
 * TODO write documentation
 */
public class Message {
    private final LocalDateTime timestamp;

    private final Object message;

    //TODO equals for message delete

    public Message(@Nonnull LocalDateTime timestamp, @Nonnull Object message) {
        //TODO check for null
        this.timestamp = timestamp;
        this.message = message;
    }

    public Message(@Nonnull Object message) {
        this(LocalDateTime.now(), message);
    }
}
