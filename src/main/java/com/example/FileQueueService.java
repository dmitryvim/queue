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
        //TODO sanitize message text on line break
        fileHandler(queueName, true).writeLine(wrapper.representation());
    }

    private String pullTransformer(String line, Consumer<Message> consumer) {
        MessageWrapper wrapper = new MessageWrapper(line);
        if (wrapper.readyForAccess()) {
            consumer.accept(wrapper.readMessage());
            return wrapper.representation();
        } else {
            return null;
        }
    }

    @CheckForNull
    @Override
    public Message pull(@Nonnull String queueName) {
        Message[] messages = new Message[1];
        FileHandler fileHandler = fileHandler(queueName, false);
        if (fileHandler == null) {
            return null;
        } else {
            fileHandler.replaceLineWith(line -> pullTransformer(line, message -> messages[0] = message));
            return messages[0];
        }
    }

    private boolean accessedMessage(String line) {
        return new MessageWrapper(line).accessed();
    }

    private boolean removePredicate(String line, Message message) {
        MessageWrapper wrapper = new MessageWrapper(line);
        return message.getHandler().equals(wrapper.handler());
    }

    @Override
    public void delete(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        FileHandler fileHandler = fileHandler(queueName, false);
        if (fileHandler != null) {
            fileHandler.removeLineWithPredicate(line -> removePredicate(line, message), this::accessedMessage);
        }
    }

    @CheckForNull
    private FileHandler fileHandler(@Nonnull String queueName, boolean shouldCreateFile) {
        Validate.notNull(queueName, "queueName is required");
        //TODO path resolve
        File file = new File(this.directory + "/" + queueName);
        if (file.exists() || shouldCreateFile) {
            return new FileHandler(file);
        } else {
            return null;
        }
    }
}
