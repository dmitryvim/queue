package com.example;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public interface QueueService {

    void push(@Nonnull String queueName, @Nonnull Message message);

    @CheckForNull
    Message pull(@Nonnull String queueName);

    void delete(@Nonnull String queueName, @Nonnull Message message);
}
