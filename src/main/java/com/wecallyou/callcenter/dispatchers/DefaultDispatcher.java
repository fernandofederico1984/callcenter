package com.wecallyou.callcenter.dispatchers;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wecallyou.callcenter.Message;
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

public class DefaultDispatcher implements Dispatcher {

    private static Logger LOG = LoggerFactory.getLogger(DefaultDispatcher.class);
    private static int WAIT_TIME = 2;

    private final ThreadPoolExecutor executorService;
    private final Employee employee;
    private final Dispatcher child;
    private Optional<BlockingQueue<Message>> waitingQueue = Optional.empty();
    private final Map<Message, MessageReport> report = new HashMap<>();

    public DefaultDispatcher(Employee employee, int numberOfEmployees, Dispatcher child) {
        this.child = child;
        this.employee = employee;
        this.executorService = new ThreadPoolExecutor(numberOfEmployees, numberOfEmployees, WAIT_TIME, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new ThreadFactoryBuilder().setNameFormat(employee.getType() + "-%d").build());
    }

    public DefaultDispatcher(Employee employee, int numberOfEmployees, Dispatcher child,
                             BlockingQueue<Message> waitingQueue) {
        this(employee, numberOfEmployees, child);
        this.waitingQueue = Optional.ofNullable(waitingQueue);
    }

    @Override
    public void dispatchCall(Message message) throws NoEmployeesAvailableException, MaximumMessagesReached {
        try {
            executorService.submit(() -> {
                doProcess(message);
                processWaitingMessages();
            });
        } catch (RejectedExecutionException e) {
            child.dispatchCall(message);
        }
    }

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

    @Override
    public boolean isDone(){
        return executorService.getActiveCount() == 0 && child.isDone();
    }


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
