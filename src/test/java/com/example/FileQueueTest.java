package com.example;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class FileQueueTest {

    @Test
    public void doubleQueueTest() throws Exception {

        //given
        String queue = "queue";
        File tempDirectory = Files.createTempDirectory("file-queue-test").toFile();
        QueueService queueService1 = new FileQueueService(tempDirectory);
        QueueService queueService2 = new FileQueueService(tempDirectory);
        Message pushed = new Message("message");

        //when
        queueService1.push(queue, pushed);
        Message pulled = queueService2.pull(queue);

        //then
        assertEquals(pushed, pulled);
    }
}
