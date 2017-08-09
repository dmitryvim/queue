package com.example;

import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.util.function.Consumer;

public class FileQueueService implements QueueService {

    /**
     * directory for queue file storage
     */
    private final File directory;

    public FileQueueService(File directory) {
        this.directory = directory;
    }

    @Override
    public void push(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        MessageWrapper wrapper = new MessageWrapper(message);
        fileHandler(queueName).writeLine(wrapper.toString());
    }

    private String pullTransformer(String line, Consumer<Message> consumer) {
        MessageWrapper wrapper = new MessageWrapper(line);
        if (wrapper.readyForAccess()) {
            consumer.accept(wrapper.readMessage());
            return wrapper.toString();
        } else {
            return null;
        }
    }

    private boolean unreadMessage(String line) {
        return new MessageWrapper(line).accessed();
    }

    @CheckForNull
    @Override
    public Message pull(@Nonnull String queueName) {
        Message[] messages = new Message[1];
        fileHandler(queueName).replaceLineWith(line -> pullTransformer(line, message -> messages[0] = message));
        return messages[0];
    }

    private boolean removePredicate(String line, Message message) {
        MessageWrapper wrapper = new MessageWrapper(line);
        return message.getHandler().equals(wrapper.handler());
    }

    @Override
    public void delete(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        fileHandler(queueName).removeLineWithPredicate(line -> removePredicate(line, message), this::unreadMessage);
    }

    private FileHandler fileHandler(@Nonnull String queueName) {
        Validate.notNull(queueName, "queueName is required");
        File file = new File(this.directory + "/" + queueName);
        return new FileHandler(file);
    }
}
