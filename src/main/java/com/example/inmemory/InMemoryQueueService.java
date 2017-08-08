package com.example.inmemory;

import com.example.queue.Message;
import com.example.queue.QueueService;
import com.google.common.base.Throwables;
import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class InMemoryQueueService implements QueueService {

    //TODO определиться с размерами очереди
    private final static int QUEUE_SIZE = 10;

    private final Map<String, BlockingQueue<Message>> queues = new HashMap<>();

    private final Object deleteMutex = new Object();

    private final Object createQueueMutex = new Object();

    @Override
    public void push(@Nonnull String queueName, @Nonnull Message message) {
        //TODO check null
        try {
            queue(queueName).put(message);
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }
    }

    @CheckForNull
    @Override
    public Message pull(@Nonnull String queueName) {
        return queue(queueName).peek();
    }

    @Override
    public void delete(@Nonnull String queueName, @Nonnull Message message) {
        BlockingQueue<Message> queue = queue(queueName);
        if (queue.peek() == message) {
            synchronized (this.deleteMutex) {
                if (queue.peek() == message) {
                    try {
                        queue.take();
                    } catch (InterruptedException e) {
                        Throwables.propagate(e);
                    }
                }
            }
        }
    }

    private BlockingQueue<Message> queue(@Nonnull String queueName) {
        Validate.notNull(queueName, "queueName is required");
        BlockingQueue<Message> queue = this.queues.get(queueName);
        if (queue == null) {
            synchronized (this.createQueueMutex) {
                if (queue == null) {
                    queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
                    this.queues.put(queueName, queue);
                }
            }
        }
        return queue;
    }
    //
    // Task 2: Implement me.
    //
}
