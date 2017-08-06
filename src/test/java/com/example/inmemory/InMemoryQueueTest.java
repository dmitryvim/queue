package com.example.inmemory;

import com.example.queue.Message;
import com.example.queue.QueueService;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class InMemoryQueueTest {
  //
  // Implement me.
  //
  private static String QUEUE_NAME = "test-queue";

    private QueueService queueService;

    @Before
    public void initQueue() {
        this.queueService = new InMemoryQueueService();
        this.queueService.createQueue(QUEUE_NAME);
    }

    @Test
    public void queueTest() {
        // given
        List<Message> messages = Arrays.asList(message("first"), message("second"), message("third"));

        // when
        this.queueService.push(QUEUE_NAME, messages.get(0));
        this.queueService.push(QUEUE_NAME, messages.get(1));

        // then
        pullDeleteAndAssertMessage(QUEUE_NAME, messages.get(0));

        // when
        this.queueService.push(QUEUE_NAME, messages.get(2));

        // then
        pullDeleteAndAssertMessage(QUEUE_NAME, messages.get(1));
        pullDeleteAndAssertMessage(QUEUE_NAME, messages.get(2));
    }

    @Test
    public void shouldReturnTheSameMessageOnRepeatedPull() {
        // given
        Arrays.asList(message("first"), message("second")).forEach(message -> this.queueService.push(QUEUE_NAME, message));

        // expect
        Message firstMessage1 = this.queueService.pull(QUEUE_NAME);
        Message firstMessage2 = this.queueService.pull(QUEUE_NAME);
        assertEquals(firstMessage1, firstMessage2);
    }

    @Test
    public void shouldReturnNullOnEmptyQueue() {
        assertNull(this.queueService.pull(QUEUE_NAME));
    }

    @Test
    public void shouldReturnQueueSpecificMessages() {

        // given
        String secondQueue = "test-2-queue";
        List<Message> messages = Arrays.asList(message("first"), message("second"));
        this.queueService.createQueue(secondQueue);

        // when
        this.queueService.push(QUEUE_NAME, messages.get(0));
        this.queueService.push(secondQueue, messages.get(1));

        // then
        Message firstQueueMessage = this.queueService.pull(QUEUE_NAME);
        Message secondQueueMessage = this.queueService.pull(secondQueue);
        assertEquals(messages.get(0), firstQueueMessage);
        assertEquals(messages.get(1), secondQueueMessage);
    }

    private void pullDeleteAndAssertMessage(String queue, Message expected) {
        Message message = this.queueService.pull(QUEUE_NAME);
        assertEquals(expected, message);
        this.queueService.delete(QUEUE_NAME, message);
    }

    private Message message(String text) {
        return new Message(text);
    }
}
