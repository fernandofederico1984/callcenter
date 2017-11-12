package com.wecallyou.callcenter.dispatchers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wecallyou.callcenter.Employee;
import com.wecallyou.callcenter.Message;
import com.wecallyou.callcenter.dispatchers.exceptions.DispatchingMessageException;
import com.wecallyou.callcenter.dispatchers.exceptions.MaximumMessagesReached;
import com.wecallyou.callcenter.dispatchers.exceptions.NoEmployeesAvailableException;
import com.wecallyou.callcenter.report.MessageReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * {@link Dispatcher} implementation that delegates the message processing to a child in case it is unavailable.
 *
 * The message is processed in the following manner:
 *
 * 1) The Message is processed by the employees enclosed in the dispatcher.
 * 2) If No employee is able to process a message, then the child is called.
 * 3) Once the employee process the message, it continues pulling messages from the waiting queue.
 */
public class DelegatingDispatcher implements Dispatcher {

    private static Logger LOG = LoggerFactory.getLogger(DelegatingDispatcher.class);
    private static int WAIT_TIME = 2;

    /**
     * Thread Pool representing the number of employees handling calls.
     */
    private final ThreadPoolExecutor executorService;

    /**
     * Employee command that process the message
     */
    private final Employee employee;

    /**
     * Child Dispatcher. If the message cannot be processed by current dispatcher, it is handled to its child.
     */
    private final Dispatcher child;

    /**
     * Optional Waiting queue representing the messages that are on hold. That has to be processed.
     */
    private Optional<BlockingQueue<Message>> waitingQueue = Optional.empty();

    /**
     * Message Report by Message. All the messages processed by current dispatcher.
     */
    private final Map<Message, MessageReport> report = new HashMap<>();


    DelegatingDispatcher(Employee employee, int numberOfEmployees, Dispatcher child) {
        this.child = child;
        this.employee = employee;
        this.executorService = new ThreadPoolExecutor(numberOfEmployees, numberOfEmployees, WAIT_TIME, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new ThreadFactoryBuilder().setNameFormat(employee.getType() + "-%d").build());
    }

    DelegatingDispatcher(Employee employee, int numberOfEmployees, Dispatcher child,
                                BlockingQueue<Message> waitingQueue) {
        this(employee, numberOfEmployees, child);
        this.waitingQueue = Optional.ofNullable(waitingQueue);
    }

    /**
     * Main Method of the {@link Dispatcher} it process a new message
     *
     * @param message The message to be processed.
     *
     * @throws NoEmployeesAvailableException In case the message cannot be processed because there are not employees
     * available.
     * @throws MaximumMessagesReached In case the maximum number of messages that are in waiting state is reached.
     * @throws NullPointerException In case the message is null.
     */
    @Override
    public void dispatchCall(Message message) throws DispatchingMessageException {
        try {
            if ( message == null ) throw new NullPointerException("Message cannot be null");

            executorService.submit(() -> {
                doProcess(message);
                processWaitingMessages();
            });
        } catch (RejectedExecutionException e) {
            child.dispatchCall(message);
        }
    }

    /**
     * Tears down the Dispatcher and its child. It waits for all the processing messages are completed.
     *
     * @param timeOut The maximum time that has to be waited in the shut down process.
     */
    @Override
    public void shutdown(long timeOut) {
        try {
            child.shutdown(timeOut);
            executorService.shutdown();
            executorService.awaitTermination(timeOut, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("The Dispatcher Could not be shut down");
        }
    }

    /**
     * Determines if all the messages has been processed.
     */
    @Override
    public boolean isDone(){
        return executorService.getActiveCount() == 0 && child.isDone();
    }

    /**
     * @return All the processed messages report, including the ones processed by childs.
     */
    @Override
    public Map<Message, MessageReport> getReport(){
        Map<Message, MessageReport> messageReport = new HashMap<>(report);
        messageReport.putAll(child.getReport());
        return Collections.unmodifiableMap(messageReport);
    }


    private void processWaitingMessages() {
        waitingQueue.ifPresent(messages -> {
            for (Message message = messages.poll(); message != null; message = messages.poll())
                doProcess(message);
        });
    }

    private void doProcess(Message message) {
        employee.process(message)
                .ifPresent(messageReport -> report.put(message, messageReport));
    }
}
