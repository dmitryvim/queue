package com.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author dmitry.mikhailovich@gmail.com
 */
@RunWith(Parameterized.class)
public class QueueServiceTest {

    @Parameterized.Parameter
    public QueueService queueService;

    @Parameterized.Parameters
    public static List<QueueService> data() throws Exception {
        Path path = Files.createTempDirectory("queue-service-test");
        return Arrays.asList(new InMemoryQueueService(), new FileQueueService(path.toFile()));
    }

    private static String randomQueueName() {
        return "queue-" + UUID.randomUUID().toString();
    }

    @Test
    public void queueTest() {
        // given
        String queueName = randomQueueName();
        List<Message> messages = Arrays.asList(message("first"), message("second"), message("third"));

        // when
        this.queueService.push(queueName, messages.get(0));
        this.queueService.push(queueName, messages.get(1));

        // then
        pullDeleteAndAssertMessage(queueName, messages.get(0));

        // when
        this.queueService.push(queueName, messages.get(2));

        // then
        pullDeleteAndAssertMessage(queueName, messages.get(1));
        pullDeleteAndAssertMessage(queueName, messages.get(2));
    }

    @Test
    public void shouldReturnTheSameMessageOnRepeatedPull() throws Exception {
        // given
        String queueName = randomQueueName();
        Arrays.asList(message("first"), message("second")).forEach(message -> this.queueService.push(queueName, message));

        // expect
        Message firstMessage1 = this.queueService.pull(queueName);
        TimeUnit.SECONDS.sleep(2);
        Message firstMessage2 = this.queueService.pull(queueName);
        assertEquals(firstMessage1, firstMessage2);
    }

    @Test
    public void shouldReturnNullOnEmptyQueue() {
        assertNull(this.queueService.pull(randomQueueName()));
    }

    @Test
    public void shouldReturnQueueSpecificMessages() {

        // given
        String firstQueue = randomQueueName();
        String secondQueue = randomQueueName();
        List<Message> messages = Arrays.asList(message("first"), message("second"));

        // when
        this.queueService.push(firstQueue, messages.get(0));
        this.queueService.push(secondQueue, messages.get(1));

        // then
        Message firstQueueMessage = this.queueService.pull(firstQueue);
        Message secondQueueMessage = this.queueService.pull(secondQueue);
        assertEquals(messages.get(0), firstQueueMessage);
        assertEquals(messages.get(1), secondQueueMessage);
    }

    private void pullDeleteAndAssertMessage(String queue, Message expected) {
        Message message = this.queueService.pull(queue);
        assertEquals(expected, message);
        this.queueService.delete(queue, message);
    }

    private Message message(String text) {
        return new Message(text);
    }
}