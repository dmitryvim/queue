package com.example.queue;

import com.example.file.FileQueueService;
import com.example.inmemory.InMemoryQueueService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author dmitry.mikhaylovich@bostongene.com
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

    private static String randomeQueueName() {
        return "queue-" + UUID.randomUUID().toString();
    }

    @Test
    public void queueTest() {
        // given
        String queueName = randomeQueueName();
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
    public void shouldReturnTheSameMessageOnRepeatedPull() {
        // given
        String queueName = randomeQueueName();
        Arrays.asList(message("first"), message("second")).forEach(message -> this.queueService.push(queueName, message));

        // expect
        Message firstMessage1 = this.queueService.pull(queueName);
        Message firstMessage2 = this.queueService.pull(queueName);
        assertEquals(firstMessage1, firstMessage2);
    }

    @Test
    public void shouldReturnNullOnEmptyQueue() {
        assertNull(this.queueService.pull(randomeQueueName()));
    }

    @Test
    public void shouldReturnQueueSpecificMessages() {

        // given
        String firstQueue = randomeQueueName();
        String secondQueue = randomeQueueName();
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