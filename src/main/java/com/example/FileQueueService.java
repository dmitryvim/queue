package com.example;

import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.ListIterator;
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

    //TODO implement string length constant message.toString() to change only one file line
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

    //TODO implement remove line with predicate in FileHandler
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
}
