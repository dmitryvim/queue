package com.example.amazon;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.example.queue.Message;
import com.example.queue.QueueService;
import org.apache.commons.lang.Validate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;

/**
 * QueueService implementation working with Amazon queue provider
 */
public class SqsQueueService implements QueueService {

    private static final int VISIBILITY_TIMEOUT = 1000;

    private final AmazonSQSClient amazonSQSClient;

    public SqsQueueService(@Nonnull AmazonSQSClient sqsClient) {
        Validate.notNull(sqsClient, "sqsClient is required");
        this.amazonSQSClient = sqsClient;
    }

    @Override
    public void push(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        String url = queueUrl(queueName);
        this.amazonSQSClient.sendMessage(url, message.getBody());
    }

    @CheckForNull
    @Override
    public Message pull(@Nonnull String queueName) {
        ReceiveMessageRequest request = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl(queueName))
                .withVisibilityTimeout(VISIBILITY_TIMEOUT)
                .withMaxNumberOfMessages(1);
        ReceiveMessageResult messageResult = this.amazonSQSClient.receiveMessage(request);
        List<com.amazonaws.services.sqs.model.Message> messages = messageResult.getMessages();
        if (messages == null || messages.isEmpty()) {
            return null;
        } else {
            com.amazonaws.services.sqs.model.Message message = messages.get(0);
            return new Message(message.getBody(), message.getReceiptHandle());
        }
    }

    @Override
    public void delete(@Nonnull String queueName, @Nonnull Message message) {
        String queueUrl = queueUrl(queueName);
        this.amazonSQSClient.deleteMessage(queueUrl, message.getHandler());
    }

    private String queueUrl(@Nonnull String queueName) {
        Validate.notEmpty(queueName, "queueName is required");
        GetQueueUrlResult getResult = this.amazonSQSClient.getQueueUrl(queueName);
        String queueUrl = getResult.getQueueUrl();
        if (queueUrl == null) {
            CreateQueueResult createResult = this.amazonSQSClient.createQueue(queueName);
            queueUrl = createResult.getQueueUrl();
        }
        return queueUrl;
    }
}
