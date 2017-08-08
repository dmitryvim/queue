package com.example.inmemory;

import com.example.queue.Message;
import com.example.queue.QueueService;
import com.google.common.base.Throwables;
import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class InMemoryQueueService implements QueueService {

    //TODO определиться с размерами очереди
    private final static int QUEUE_SIZE = 10;

    private final static int INVISIBLE_FOR_READ_TIMEOUT = 1000;

    private final Map<String, ArrayBlockingQueue<MessageWrapper>> queues = new HashMap<>();

    private final Object deleteMutex = new Object();

    private final Object createQueueMutex = new Object();

    @Override
    public void push(@Nonnull String queueName, @Nonnull Message message) {
        //TODO check null
        try {
            queue(queueName).put(new MessageWrapper(message));
        } catch (InterruptedException e) {
            Throwables.propagate(e);
        }
    }

    @CheckForNull
    @Override
    public Message pull(@Nonnull String queueName) {

        BlockingQueue<MessageWrapper> queue = queue(queueName);
        return queue.stream()
                .filter(MessageWrapper::readyForAccess)
                .findFirst()
                .map(MessageWrapper::readMessage)
                .orElse(null);
    }

    @Override
    //TODO
    public void delete(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        ArrayBlockingQueue<MessageWrapper> queue = queue(queueName);
        Iterator<MessageWrapper> iterator = queue.iterator();
        MessageWrapper next;
        while (iterator.hasNext() && (next = iterator.next()).accessed()) {
            if (next.handler().equals(message.getHandler())) {
                iterator.remove();
            }
        }
//        if (queue.peek().getHandler().equals(message.getHandler())) {
//            synchronized (this.deleteMutex) {
//                if (queue.peek() == message) {
//                    try {
//                        queue.take();
//                    } catch (InterruptedException e) {
//                        Throwables.propagate(e);
//                    }
//                }
//            }
//        }
    }

    private ArrayBlockingQueue<MessageWrapper> queue(@Nonnull String queueName) {
        Validate.notNull(queueName, "queueName is required");
        ArrayBlockingQueue<MessageWrapper> queue = this.queues.get(queueName);
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

    private static class MessageWrapper {
        private final Message message;

        private long lastAccess = 0;

        MessageWrapper(Message message) {
            this.message = message;
        }

        Message readMessage() {
            this.lastAccess = Instant.now().toEpochMilli();
            return message;
        }

        boolean readyForAccess() {
            long now = Instant.now().toEpochMilli();
            return (this.lastAccess == 0) || (now - this.lastAccess < INVISIBLE_FOR_READ_TIMEOUT);
        }

        boolean accessed() {
            return this.lastAccess > 0;
        }

        String handler() {
            return this.message.getHandler();
        }
    }

    //
    // Task 2: Implement me.
    //
}
