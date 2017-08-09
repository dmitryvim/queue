package com.example;

import java.time.Instant;

/**
 * message wrapper for InMemory- and File- QueueService implementations
 */
class MessageWrapper {

    /**
     * timeout the file invisible for repeated pulling
     */
    private final static int INVISIBLE_FOR_READ_TIMEOUT_IN_MS = 1000;

    private final Message message;

    private long lastAccess = 0;

    MessageWrapper(Message message) {
        this.message = message;
    }

    /**
     * MessageWrapper can be transformed to representation line and deserialized from it
     * line format $access_time_in_milliseconds:$handler:$message_text
     */
    MessageWrapper(String representation) {
        String[] lines = representation.trim().split(":", 3);
        if (lines.length < 3) {
            throw new IllegalArgumentException("Unable to read representation line.");
        } else {
            this.lastAccess = Long.valueOf(lines[0]);
            this.message = new Message(lines[2], lines[1]);
        }
    }

    Message readMessage() {
        this.lastAccess = Instant.now().toEpochMilli();
        return message;
    }

    boolean readyForAccess() {
        long now = Instant.now().toEpochMilli();
        return (this.lastAccess == 0) || (now - this.lastAccess > INVISIBLE_FOR_READ_TIMEOUT_IN_MS);
    }

    boolean accessed() {
        return this.lastAccess > 0;
    }

    String handler() {
        return this.message.getHandler();
    }

    public String representation() {
        // 13 is current digit count of Instant.now().toEpochMilli();
        return String.format("%15d:%s:%s", this.lastAccess, this.message.getHandler(), this.message.getBody());
    }
}