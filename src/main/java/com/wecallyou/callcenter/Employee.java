package com.wecallyou.callcenter;

import com.wecallyou.callcenter.report.MessageReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Employee representation.
 */
public class Employee implements Comparable<Employee> {
    private static Logger LOG = LoggerFactory.getLogger(Employee.class);

    /**
     * The type of the employee
     */
    private EmployeeType type;

    /**
     * Maximum processing time.
     */
    private int maxTime;

    /**
     * Minimum processing time.
     */
    private int minTime;

    private Integer priority;

    public static Employee operator(int maxTime, int minTime) {
        return new Employee(EmployeeType.OPERATOR, maxTime, minTime,0);
    }

    public static Employee supervisor(int maxTime, int minTime) {
        return new Employee(EmployeeType.SUPERVISOR, maxTime, minTime,2);
    }

    public static Employee director(int maxTime, int minTime) {
        return new Employee(EmployeeType.DIRECTOR, maxTime, minTime,4);
    }


    public Optional<MessageReport> process(Message message) {
        try {
            LOG.debug("{} processing message: {}", Thread.currentThread().getName(), message.getOrder());
            long processTime = waitingTime();
            Thread.sleep(processTime);
            LOG.debug("{} finished processing message: {}", Thread.currentThread().getName(), message.getOrder());
            return Optional.of(new MessageReport(message,(int) (processTime/1000), type ));
        } catch (InterruptedException e) {
            LOG.error("The Thread was interrupted, customer communication was shut down");
        }
        return Optional.empty();
    }

    private Employee(EmployeeType type, int maxTime, int minTime, int priority) {
        this.type = type;
        this.maxTime = maxTime;
        this.minTime = minTime;
        this.priority = priority;
    }



    public EmployeeType getType() {
        return type;
    }

    private long waitingTime() {
        return ((long) (Math.random() * (maxTime - minTime)) + minTime) * 1000;
    }

    @Override
    public int compareTo(Employee o) {
        return priority.compareTo(o.priority);
    }
}
