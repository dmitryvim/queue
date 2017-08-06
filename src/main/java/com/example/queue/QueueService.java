package com.example.queue;

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

    void push(@Nonnull String queueName, @Nonnull Message message);

    @CheckForNull
    Message pull(@Nonnull String queueName);

    void delete(@Nonnull String queueName, @Nonnull Message message);

    void createQueue(@Nonnull String name);

    void deleteQueue(@Nonnull String name);

    /*
    //TODO //FIXME

      1. multi-tread testing
      2. file based implementation
      3. amazon based implementation
      3.1. read about amazon queue
      3.2. register to amazon sqs
      3.3. mock amazon sqs

     */
}
