package com.example;

import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class InMemoryQueueService implements QueueService {

    private final static int QUEUE_SIZE = 1000;

    private final Map<String, BlockingQueue<MessageWrapper>> queues = new HashMap<>();

    private final Object createQueueMutex = new Object();

    @Override
    public void push(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        try {
            queue(queueName, true).put(new MessageWrapper(message));
        } catch (InterruptedException e) {
            //TODO bad practice
            throw new RuntimeException("interrupted", e);
        }
    }

    @CheckForNull
    @Override
    public Message pull(@Nonnull String queueName) {
        BlockingQueue<MessageWrapper> queue = queue(queueName, false);
        return queue == null ? null : queue.stream()
                .filter(MessageWrapper::readyForAccess)
                .findFirst()
                //TODO race condition in queue(), double-checking is missing a 2nd queues.get(queueName)
                // need lock message wrapper in readMessage
                .map(MessageWrapper::readMessage)
                .orElse(null);
    }

    @Override
    public void delete(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        BlockingQueue<MessageWrapper> queue = queue(queueName, false);
        if (queue != null) {
            Iterator<MessageWrapper> iterator = queue.iterator();
            MessageWrapper next;
            while (iterator.hasNext() && (next = iterator.next()).accessed()) {
                if (next.handler().equals(message.getHandler())) {
                    iterator.remove();
                }
            }
        }
    }

    @CheckForNull
    private BlockingQueue<MessageWrapper> queue(@Nonnull String queueName, boolean shouldCreateQueue) {
        Validate.notNull(queueName, "queueName is required");
        BlockingQueue<MessageWrapper> queue = this.queues.get(queueName);
        if (queue == null && shouldCreateQueue) {
            synchronized (this.createQueueMutex) {
                if (queue == null) {
                    queue = new ArrayBlockingQueue<>(QUEUE_SIZE);
                    this.queues.put(queueName, queue);
                }
            }
        }
        return queue;
    }
}
