package com.example.file;

import com.example.queue.Message;
import com.example.queue.QueueService;
import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.File;

public class FileQueueService implements QueueService {

    private final File directory;

    public FileQueueService(File directory) {
        this.directory = directory;
    }

    @Override
    public void push(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        fileWorker(queueName).writeLine(message.line());
    }

    @CheckForNull
    @Override
    public Message pull(@Nonnull String queueName) {
        String line = fileWorker(queueName).readFirstLine();
        return line == null ? null : Message.of(line);
    }

    @Override
    public void delete(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        fileWorker(queueName).removeFirstLine(message.line());
    }

    private FileWorker fileWorker(@Nonnull String queueName) {
        Validate.notNull(queueName, "queueName is required");
        File file = new File(this.directory + "/" + queueName);
        return new FileWorker(file);
    }

    //
  // Task 3: Implement me if you have time.
  //
}
