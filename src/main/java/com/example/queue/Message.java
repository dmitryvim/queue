package com.example.queue;

import org.apache.commons.lang.Validate;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * TODO write documentation
 */
public class Message {
    private final LocalDateTime timestamp;

    private final String message;

    private final String id;

    private Map<String, String> attributes;

    //TODO equals for message delete

    public Message(@Nonnull String id, @Nonnull String message, @Nonnull LocalDateTime timestamp) {
        Validate.notEmpty(id, "id is required");
        Validate.notEmpty(message, "message is required");
        Validate.notNull(timestamp, "timestamp is required");
        this.id = id;
        this.message = message;
        this.timestamp = timestamp;
    }

    // added only for amazon sqs implementation
    public Message withAttribute(@Nonnull String name, @Nonnull String value) {
        Validate.notEmpty(name, "name is required");
        Validate.notEmpty(value, "value is required");
        if (this.attributes == null) {
            this.attributes = new HashMap<>();
        }
        this.attributes.put(name, value);
        return this;
    }

    public String getAttribute(@Nonnull String name) {
        Validate.notEmpty(name, "name is required");
        return (this.attributes == null) ? null : this.attributes.get(name);
    }

    public Message(@Nonnull String message) {
        this(UUID.randomUUID().toString(), message, LocalDateTime.now());
    }

    public static Message of(@Nonnull String line) {
        Validate.notNull(line, "line is required");
        String[] strings = line.split(":", 3);
        long millis = Long.valueOf(strings[1]);
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC);
        //TODO encode decode message
        return new Message(strings[0], strings[2], time);
    }


    public String line() {
        //TODO encode decode message
        return this.id + ":" + this.timestamp.toInstant(ZoneOffset.UTC).toEpochMilli() + ":" + message;
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
