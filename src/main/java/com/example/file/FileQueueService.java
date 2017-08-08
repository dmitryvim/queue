package com.example.file;

import com.example.queue.Message;
import com.example.queue.QueueService;

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

    }

    @CheckForNull
    @Override
    public Message pull(@Nonnull String queueName) {
        return null;
    }

    @Override
    public void delete(@Nonnull String queueName, @Nonnull Message message) {

    }

    //
  // Task 3: Implement me if you have time.
  //
}
