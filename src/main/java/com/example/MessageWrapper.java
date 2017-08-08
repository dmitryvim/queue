package com.example;

import java.time.Instant;

/**
 * message wrapper for InMemory- and File- QueueService implemenatations
 */
class MessageWrapper {

    /**
     * timeout the file invisible for repeated pulling
     */
    private final static int INVISIBLE_FOR_READ_TIMEOUT = 1000;

    private final Message message;

    private long lastAccess = 0;

    MessageWrapper(Message message) {
        this.message = message;
    }

    MessageWrapper(String line) {
        String[] lines = line.trim().split(":", 3);
        this.lastAccess = Long.valueOf(lines[0]);
        this.message = new Message(lines[2], lines[1]);
    }

    Message readMessage() {
        this.lastAccess = Instant.now().toEpochMilli();
        return message;
    }

    boolean readyForAccess() {
        long now = Instant.now().toEpochMilli();
        return (this.lastAccess == 0) || (now - this.lastAccess > INVISIBLE_FOR_READ_TIMEOUT);
    }

    boolean accessed() {
        return this.lastAccess > 0;
    }

    String handler() {
        return this.message.getHandler();
    }

    @Override
    public String toString() {
        // 13 is current digit count of Instant.now().toEpochMilli();
        return String.format("%15d:%s:%s", this.lastAccess, this.message.getHandler(), this.message.getBody());
    }
}