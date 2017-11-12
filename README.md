# Callcenter

There are 2 types of Message dispatcher:

* Fail on max: Fails when all the operators/supervisors/directors are occupied.
* Queue dispatcher: This queues up the messages process them when an operator/supervisor/director is free up. In this last case, the order is not guarantee, although it is unlikely the message can be queued up at the same time an operator is free up. So the message might not be processed by an operator/director/supervisor. In that case there is a guaranty process dispatcher that covers the scenarios of eventual processing.

This code has the concept of:

* Dispatcher: Receives a message and dispatch it to an employee
* Employee: Is the one that process the message. (operator/supervisor/director)
* Message report: Report status of a processed message.
