package com.example.file;

import com.example.queue.Message;
import com.example.queue.QueueService;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileQueueTest {
    //
    // Implement me if you have time.
    //

    private QueueService queueService;

    @Before
    public void initQueue() throws Exception {
        Path tempDirectory = Files.createTempDirectory("file-queue-test");

        this.queueService = new FileQueueService(tempDirectory.toFile());
    }

    @Test
    public void queueTest() {
        //TODO load test
    }

    private Message message(String text) {
        return new Message(text);
    }
}
