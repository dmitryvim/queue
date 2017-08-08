package com.example.file;

import com.example.queue.Message;
import com.example.queue.QueueService;
import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

public class FileQueueService implements QueueService {

    private final File directory;

    private final static int INVISIBLE_FOR_READ_TIMEOUT = 1000;

    public FileQueueService(File directory) {
        this.directory = directory;
    }

    @Override
    public void push(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        MessageWrapper wrapper = new MessageWrapper(message);
        fileHandler(queueName).writeLine(wrapper.toString());
    }

    private List<String> pullTransform(List<String> lines, Consumer<Message> consumer) {
        Message message = null;
        ListIterator<String> iterator = lines.listIterator();
        while (iterator.hasNext() && message == null) {
            MessageWrapper wrapper = new MessageWrapper(iterator.next());
            if (wrapper.readyForAccess()) {
                message = wrapper.readMessage();
                iterator.set(wrapper.toString());
            }
        }
        if (message != null) {
            consumer.accept(message);
        }
        return lines;
    }

    @CheckForNull
    @Override
    public Message pull(@Nonnull String queueName) {
        Message[] messages = new Message[1];
        fileHandler(queueName).transform(lines -> pullTransform(lines, message -> messages[0] = message));
        return messages[0];
    }

    private boolean predicate(String line, Message message) {
        MessageWrapper wrapper = new MessageWrapper(line);
        return wrapper.accessed() && wrapper.handler().equals(message.getHandler());
    }

    @Override
    public void delete(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        fileHandler(queueName).transform(lines -> {
            lines.removeIf(line -> predicate(line, message));
            return lines;
        });
    }

    private FileHandler fileHandler(@Nonnull String queueName) {
        Validate.notNull(queueName, "queueName is required");
        File file = new File(this.directory + "/" + queueName);
        return new FileHandler(file);
    }

    private static class MessageWrapper {

        private final Message message;

        private long lastAccess = 0;

        MessageWrapper(Message message) {
            this.message = message;
        }

        MessageWrapper(String line) {
            String[] lines = line.split(":", 3);
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
            return String.valueOf(this.lastAccess) + ":" + this.message.getHandler() + ":" + this.message.getBody();
        }
    }
    //
  // Task 3: Implement me if you have time.
  //
}
