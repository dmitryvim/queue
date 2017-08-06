package com.example;

import com.amazonaws.services.sqs.AmazonSQSClient;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

public class SqsQueueService implements QueueService {
  //
  // Task 4: Optionally implement parts of me.
  //
  // This file is a placeholder for an AWS-backed implementation of QueueService.  It is included
  // primarily so you can quickly assess your choices for method signatures in QueueService in
  // terms of how well they map to the implementation intended for a production environment.
  //

  public SqsQueueService(AmazonSQSClient sqsClient) {
  }

  @Override
  public void push(@Nonnull String queueName, @Nonnull Message message) {

  }

  @CheckForNull
  @Override
  public Message pull(@Nonnull String queueName) {
    return null;
  }

  @Override
  public void delete(@Nonnull String queueName, @Nonnull Message message) {

  }

  @Override
  public void createQueue(@Nonnull String name) {

  }

  @Override
  public void deleteQueue(@Nonnull String name) {

  }
}
