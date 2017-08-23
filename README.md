# Behavior

If message queues are an unfamiliar concept, see SQS docs for a description of
how they are intended to behave.  In particular, note the following properties:

 - multiplicity
   A queue supports many producers and many consumers.

 - delivery
   A queue strives to deliver each message exactly once to exactly one consumer,
   but guarantees at-least once delivery (it can re-deliver a message to a
   consumer, or deliver a message to multiple consumers, in rare cases).

 - order
   A queue strives to deliver messages in FIFO order, but makes no guarantee
   about delivery order.

 - reliability
   When a consumer receives a message, it is not removed from the queue.
   Instead, it is temporarily suppressed (becomes "invisible").  If the consumer
   that received the message does not subsequently delete it within within a
   timeout period (the "visibility timeout"), the message automatically becomes
   visible at the head of the queue again, ready to be delivered to another
   consumer.


 
