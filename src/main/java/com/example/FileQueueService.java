package com.example;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class FileQueueService implements QueueService {
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

    @Override
    public void createQueue(@Nonnull String name) {

    }

    @Override
    public void deleteQueue(@Nonnull String name) {

    }
    //
  // Task 3: Implement me if you have time.
  //
}
