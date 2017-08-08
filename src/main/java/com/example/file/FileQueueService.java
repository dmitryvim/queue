package com.example.file;

import com.example.queue.Message;
import com.example.queue.QueueService;
import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.time.Instant;

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
        fileWorker(queueName).writeLine(wrapper.line());
    }

    @CheckForNull
    @Override
    public Message pull(@Nonnull String queueName) {
        String line = fileWorker(queueName).readFirstLine();
        return line == null ? null : new MessageWrapper(line).readMessage();
    }

    @Override
    public void delete(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        fileWorker(queueName).removeFirstLine(new MessageWrapper(message).line());
    }

    private FileWorker fileWorker(@Nonnull String queueName) {
        Validate.notNull(queueName, "queueName is required");
        File file = new File(this.directory + "/" + queueName);
        return new FileWorker(file);
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
            return (this.lastAccess == 0) || (now - this.lastAccess < INVISIBLE_FOR_READ_TIMEOUT);
        }

        boolean accessed() {
            return this.lastAccess > 0;
        }

        String handler() {
            return this.message.getHandler();
        }

        String line() {
            return String.valueOf(this.lastAccess) + ":" + this.message.getHandler() + ":" + this.message.getBody();
        }
    }
    //
  // Task 3: Implement me if you have time.
  //
}
