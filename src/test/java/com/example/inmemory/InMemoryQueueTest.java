package com.example.inmemory;

import com.example.queue.Message;
import com.example.queue.QueueService;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

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
    public void shouldReturnSameValueAfterTimeout() throws Exception {
        // given
        String queue = "test-queue";
        Stream.of("first", "second", "third")
                .map(Message::new)
                .forEach(message -> this.queueService.push(queue, message));

        // when
        Message first = this.queueService.pull(queue);
        TimeUnit.SECONDS.sleep(2);
        Message second = this.queueService.pull(queue);

        // then
        assertEquals(first, second);
    }

    private Message message(String text) {
        return new Message(text);
    }
}
