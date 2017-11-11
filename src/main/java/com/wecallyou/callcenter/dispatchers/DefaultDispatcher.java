package com.wecallyou.callcenter.dispatchers;

import com.wecallyou.callcenter.EmployeeType;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.wecallyou.callcenter.EmployeeType.DIRECTOR;
import static com.wecallyou.callcenter.EmployeeType.OPERATOR;
import static com.wecallyou.callcenter.EmployeeType.SUPERVISOR;

public class DefaultDispatcher implements Dispatcher {

    private static Logger LOG = LoggerFactory.getLogger(DefaultDispatcher.class);
    private static int WAIT_TIME = 2;

    private final Dispatcher child;
    private final ThreadPoolExecutor executorService;


    public DefaultDispatcher(EmployeeType employeeType, int numberOfEmployees, Dispatcher child) {
        this.child = child;
        this.executorService = new ThreadPoolExecutor(numberOfEmployees, numberOfEmployees, WAIT_TIME, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new ThreadFactoryBuilder().setNameFormat(employeeType + "-%d").build());
    }

    public void dispatchCall(int message) throws NoEmployeesAvailableException {
        try {
            executorService.submit(() -> {
                try {
                    LOG.debug("{} processing message: {}", Thread.currentThread().getName(), message);
                    Thread.sleep(waitingTime());

                } catch (InterruptedException e) {
                    LOG.error("The Thread was interrupted, customer communication was shut down");
                }
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

    private long waitingTime() {
        return ((long) (Math.random() * 5) + 10) * 1000;
    }



}
