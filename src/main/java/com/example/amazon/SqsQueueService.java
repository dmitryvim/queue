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

public class SqsQueueService implements QueueService {
    //
    // Task 4: Optionally implement parts of me.
    //
    // This file is a placeholder for an AWS-backed implementation of QueueService.  It is included
    // primarily so you can quickly assess your choices for method signatures in QueueService in
    // terms of how well they map to the implementation intended for a production environment.
    //

    private static final String HANDLER_ATTRIBUTE_NAME = "amazon-sqs-handler";

    private final AmazonSQSClient amazonSQSClient;

    public SqsQueueService(@Nonnull AmazonSQSClient sqsClient) {
        Validate.notNull(sqsClient, "sqsClient is required");
        this.amazonSQSClient = sqsClient;
    }

    @Override
    public void push(@Nonnull String queueName, @Nonnull Message message) {
        Validate.notNull(message, "message is required");
        String url = queueUrl(queueName);
        this.amazonSQSClient.sendMessage(url, message.line());
    }

    @CheckForNull
    @Override
    public Message pull(@Nonnull String queueName) {
        ReceiveMessageRequest request = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl(queueName))
                .withMaxNumberOfMessages(1);
        ReceiveMessageResult messageResult = this.amazonSQSClient.receiveMessage(request);
        List<com.amazonaws.services.sqs.model.Message> messages = messageResult.getMessages();
        if (messages == null || messages.isEmpty()) {
            return null;
        } else {
            com.amazonaws.services.sqs.model.Message message = messages.get(0);
            return Message.of(message.getBody())
                    .withAttribute(HANDLER_ATTRIBUTE_NAME, message.getReceiptHandle());
        }
    }

    @Override
    public void delete(@Nonnull String queueName, @Nonnull Message message) {
        //TODO add message handler
        String queueUrl = queueUrl(queueName);
        this.amazonSQSClient.deleteMessage(queueUrl, message.getAttribute(HANDLER_ATTRIBUTE_NAME));
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
