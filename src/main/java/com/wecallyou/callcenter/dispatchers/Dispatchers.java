package com.wecallyou.callcenter.dispatchers;

import com.wecallyou.callcenter.Employee;

import java.util.concurrent.PriorityBlockingQueue;

import static com.wecallyou.callcenter.Employee.*;

/**
 * Factory class to create dispatchers
 */
public class Dispatchers {

    /**
     * Simple implementation of a dispatcher. Fails in case there is no employee available to dispatch a message.
     * The processing time of each employee is between minProcessingTime and maxProcessingTime seconds.
     *
     * @param numberOfOperators   The number of operators in the call center
     * @param numberOfSupervisors The number of supervisors in the call center
     * @param numberOfDirectors   The number of directors in the call center
     * @param minProcessingTime   The minimum processing time
     * @param maxProcessingTime   The maximum processing time
     * @return a {@link Dispatcher}
     */
    public static Dispatcher rejectCallsDispatcher(int numberOfOperators,
                                                             int numberOfSupervisors,
                                                             int numberOfDirectors,
                                                             int minProcessingTime,
                                                             int maxProcessingTime) {
        PriorityBlockingQueue<Employee> employees = createEmployees(numberOfOperators, numberOfSupervisors,
                numberOfDirectors, minProcessingTime, maxProcessingTime);

        return DefaultDispatcher.rejectCallsDispatcher(employees);

    }


    /**
     * Dispatcher that does not guarantee order of messages, but guarantees delivery. In case message cannot be processed
     * it goes to a waiting state.
     *
     * @param numberOfOperators   The number of operators in the call center
     * @param numberOfSupervisors The number of supervisors in the call center
     * @param numberOfDirectors   The number of directors in the call center
     * @param minProcessingTime   The minimum processing time
     * @param maxProcessingTime   The maximum processing time
     * @return a {@link Dispatcher}
     */
    public static Dispatcher nonOrderGuaranteeDispatcher(int numberOfOperators,
                                                         int numberOfSupervisors,
                                                         int numberOfDirectors,
                                                         int minProcessingTime,
                                                         int maxProcessingTime) {
        PriorityBlockingQueue<Employee> employees = createEmployees(numberOfOperators, numberOfSupervisors,
                numberOfDirectors, minProcessingTime, maxProcessingTime);

        return DefaultDispatcher.queueCallsDispatcher(employees);
    }

    private static PriorityBlockingQueue<Employee> createEmployees(int numberOfOperators, int numberOfSupervisors, int numberOfDirectors, int minProcessingTime, int maxProcessingTime) {
        PriorityBlockingQueue<Employee> employees = new PriorityBlockingQueue<>();
        for ( int i=0; i< numberOfOperators; i++) employees.offer(operator(minProcessingTime, maxProcessingTime));
        for ( int i=0; i< numberOfSupervisors; i++) employees.offer(supervisor(minProcessingTime, maxProcessingTime));
        for ( int i=0; i< numberOfDirectors; i++) employees.offer(director(minProcessingTime, maxProcessingTime));
        return employees;
    }

}
