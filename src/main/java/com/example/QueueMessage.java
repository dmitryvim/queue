package com.example;

import java.time.LocalDateTime;

/**
 * Created by dmitry on 05.08.17. ${PATH}
 */
public class QueueMessage {
    private final String queName;

    private final LocalDateTime timestamp;

    private final Object message;

    public QueueMessage(String queName, LocalDateTime timestamp, Object message) {
        this.queName = queName;
        this.timestamp = timestamp;
        this.message = message;
    }
}
