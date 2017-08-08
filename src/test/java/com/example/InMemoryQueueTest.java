package com.example;

import org.junit.Before;
import org.junit.Test;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

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

    //TODO add multi tread test
}
