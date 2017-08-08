package com.example.inmemory;

import com.example.queue.Message;
import com.example.queue.QueueService;
import org.junit.Before;
import org.junit.Test;

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
    public void queueTest() {
        //TODO load test
    }

    private Message message(String text) {
        return new Message(text);
    }
}
