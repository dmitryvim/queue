package com.example.inmemory;

import com.example.queue.Message;
import com.example.queue.QueueService;
import com.google.common.base.Throwables;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class InMemoryQueueService implements QueueService {

    //TODO определиться с размерами очереди
    private final static int QUEUE_SIZE = 10;

    private final Map<String, BlockingQueue<Message>> queues = new HashMap<>();

    private final Object deleteMutex = new Object();

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

    @Override
    public void createQueue(@Nonnull String name) {
        this.queues.put(name, new ArrayBlockingQueue<>(QUEUE_SIZE));
    }

    @Override
    public void deleteQueue(@Nonnull String name) {
        BlockingQueue<Message> queue = this.queues.get(name);
        if (queue != null) {
            //TODO проблемы с многопоточностью
            if (queue.isEmpty()) {
                this.queues.remove(name);
            } else {
                throw new IllegalStateException("Unable to delete queue while it is not empty.");
            }
        }
    }

    private BlockingQueue<Message> queue(@Nonnull String queueName) {
        return Optional.of(queueName)
                .map(this.queues::get)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find queue with name " + queueName + "."));
    }
    //
    // Task 2: Implement me.
    //
}
