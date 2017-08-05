package com.example;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public interface QueueService {

    //
    // Task 1: Define me.
    //
    // This interface should include the following methods.  You should choose appropriate
    // signatures for these methods that prioritise simplicity of implementation for the range of
    // intended implementations (in-memory, file, and SQS).  You may include additional methods if
    // you choose.
    //
    // - push
    //   pushes a message onto a queue.
    // - pull
    //   retrieves a single message from a queue.
    // - delete
    //   deletes a message from the queue that was received by pull().
    //

    public void push(@Nonnull String queueName, @Nonnull Message message);

    @CheckForNull
    public Message pull(@Nonnull String queueName);

    public void delete(@Nonnull String queueName, @Nonnull Message message);

    public void createQueue(@Nonnull String name);

    public void deleteQueue(@Nonnull String name);

}
