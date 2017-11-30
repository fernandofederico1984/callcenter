package com.wecallyou.callcenter.dispatchers;

import com.wecallyou.callcenter.Employee;
import com.wecallyou.callcenter.Message;
import com.wecallyou.callcenter.dispatchers.exceptions.DispatchingMessageException;
import com.wecallyou.callcenter.dispatchers.exceptions.NoEmployeesAvailableException;
import com.wecallyou.callcenter.report.MessageReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DefaultDispatcher implements Dispatcher {

    private static Logger LOG = LoggerFactory.getLogger(DefaultDispatcher.class);

    private final PriorityBlockingQueue<Employee> employees;
    private final ThreadPoolExecutor executorService;
    private final Map<Message, MessageReport> report = new ConcurrentHashMap<>();
    private final int employeesNumber;

    public static DefaultDispatcher queueCallsDispatcher(PriorityBlockingQueue<Employee> employees){
       return new DefaultDispatcher(employees, (ThreadPoolExecutor) Executors.newFixedThreadPool(employees.size()));
    }

    public static DefaultDispatcher rejectCallsDispatcher(PriorityBlockingQueue<Employee> employees){
        return new DefaultDispatcher(employees, new ThreadPoolExecutor(employees.size(), employees.size(), 2,
                TimeUnit.SECONDS, new SynchronousQueue<>()));
    }

    private DefaultDispatcher(PriorityBlockingQueue<Employee> employees, ThreadPoolExecutor executorService) {
        this.employees = employees;
        this.employeesNumber = employees.size();
        this.executorService = executorService;
    }
    
    @Override
    public void dispatchCall(Message message) throws DispatchingMessageException {
        try {
            executorService.submit(() -> doProcess(message));
        }
        catch (RejectedExecutionException e){
            throw new NoEmployeesAvailableException();
        }
    }

    @Override
    public void shutdown(long timeOut) {
        try {
            executorService.shutdown();
            executorService.awaitTermination(timeOut, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("The Dispatcher Could not be shut down");
        }
    }

    @Override
    public boolean isDone() {
        return executorService.getActiveCount() == 0 && executorService.getQueue().size() == 0
                && employees.size() == employeesNumber;
    }

    @Override
    public Map<Message, MessageReport> getReport() {
        return new HashMap<>(report);
    }

    private void doProcess(Message message) {
        Employee employee = employees.poll();
        try{
            employee.process(message)
                    .ifPresent(messageReport -> report.put(message, messageReport));
        }
        finally {
            employees.offer(employee);
        }
    }
}
