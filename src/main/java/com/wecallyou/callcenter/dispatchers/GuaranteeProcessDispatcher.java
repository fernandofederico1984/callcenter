package com.wecallyou.callcenter.dispatchers;

import com.wecallyou.callcenter.Message;
import com.wecallyou.callcenter.dispatchers.exceptions.DispatchingMessageException;
import com.wecallyou.callcenter.report.MessageReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Dispatcher that guarantees the processing of all the messages that are queued up.
 *
 * This covers the scenario where the message is being queued up while all the employees are freeing up.
 */
public class GuaranteeProcessDispatcher implements Dispatcher{

    private static Logger LOG = LoggerFactory.getLogger(DelegatingDispatcher.class);

    /**
     * Child Dispatcher. If the message cannot be processed by current dispatcher, it is handled to its child.
     */
    private final Dispatcher child;

    /**
     * Waiting queue representing the messages that are on hold. That has to be processed.
     */
    private final BlockingQueue<Message> waitingQueue;

    /**
     * Thread that is checking that there are not messages in the queue to process.
     */
    private final ScheduledExecutorService guaranteeDeliveryExecutor = Executors.newSingleThreadScheduledExecutor();

    public GuaranteeProcessDispatcher(Dispatcher child, BlockingQueue<Message> waitingQueue) {
        this.child = child;
        this.waitingQueue = waitingQueue;
        guaranteeDeliveryExecutor.schedule(() -> processWaitingMessages(), 2, TimeUnit.SECONDS);
    }

    @Override
    public void dispatchCall(Message message) throws DispatchingMessageException {
        child.dispatchCall(message);
    }

    @Override
    public void shutdown(long timeOut) {
        try {
            child.shutdown(timeOut);
            guaranteeDeliveryExecutor.shutdown();
            guaranteeDeliveryExecutor.awaitTermination(timeOut, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("The Dispatcher Could not be shut down");
        }
    }

    @Override
    public boolean isDone() {
        return child.isDone() && waitingQueue.isEmpty();
    }

    @Override
    public Map<Message, MessageReport> getReport() {
        return child.getReport();
    }

    private void processWaitingMessages() {
        for (Message message = waitingQueue.poll(); message != null; message = waitingQueue.poll()){
            try {
                child.dispatchCall(message);
            } catch (DispatchingMessageException e) {
                waitingQueue.offer(message);
                break;
            }

        }
    }
}
