package com.example;

import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * the big part of tests for FileQueueService and InmemoryQueueService are the same,
 * so I've moved them to QueueServiceTest
 */
public class InMemoryQueueTest {
    //
    // Implement me.
    //
    private static String QUEUE_NAME = "test-queue";

    private QueueService queueService;

    @Before
    public void initQueue() {
        this.queueService = new InMemoryQueueService();
    }

    @Test
    public void shouldReturnDifferentMessageInShortTime() {
        // given
        String queue = "test-queue";
        Stream.of("first", "second", "third")
                .map(Message::new)
                .forEach(message -> this.queueService.push(queue, message));

        // when
        Message first = this.queueService.pull(queue);
        Message second = this.queueService.pull(queue);

        // then
        assertEquals("first", first.getBody());
        assertEquals("second", second.getBody());
    }

    @Test
    public void shouldPushPullAllMessages() throws Exception {
        // given
        int size = 1000;
        String queue = "multi";

        // init pushed messages
        List<Message> pushed = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            pushed.add(new Message("message-" + i));
        }
        Set<Message> pulled = Collections.synchronizedSet(new HashSet<>());
        ExecutorService service = Executors.newCachedThreadPool();
        Runnable reader = () -> {
            while (pulled.size() < size) {
                Message message = this.queueService.pull(queue);
                if (message != null) {
                    pulled.add(message);
                    this.queueService.delete(queue, message);
                    System.out.println(message.getBody());
                }
            }
        };

        //when
        service.submit(reader);
        service.submit(reader);
        pushed.forEach(message -> this.queueService.push(queue, message));
        TimeUnit.SECONDS.sleep(10);
        assertTrue(pulled.containsAll(pushed));
    }
}
