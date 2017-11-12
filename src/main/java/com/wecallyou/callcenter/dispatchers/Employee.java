package com.wecallyou.callcenter.dispatchers;

import com.wecallyou.callcenter.EmployeeType;
import com.wecallyou.callcenter.Message;
import com.wecallyou.callcenter.report.MessageReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Employee {
    private static Logger LOG = LoggerFactory.getLogger(Employee.class);

    private EmployeeType type;
    private int maxTime;
    private int minTime;

    public static Employee operator(int maxTime, int minTime) {
        return new Employee(EmployeeType.OPERATOR, maxTime, minTime);
    }

    public static Employee supervisor(int maxTime, int minTime) {
        return new Employee(EmployeeType.SUPERVISOR, maxTime, minTime);
    }

    public static Employee director(int maxTime, int minTime) {
        return new Employee(EmployeeType.DIRECTOR, maxTime, minTime);
    }

    private Employee(EmployeeType type, int maxTime, int minTime) {
        this.type = type;
        this.maxTime = maxTime;
        this.minTime = minTime;
    }

    public Optional<MessageReport> process(Message message) {
        try {
            LOG.debug("{} processing message: {}", Thread.currentThread().getName(), message.getOrder());
            long processTime = waitingTime();
            Thread.sleep(processTime);
            return Optional.of(new MessageReport(message,(int) (processTime/1000), type ));
        } catch (InterruptedException e) {
            LOG.error("The Thread was interrupted, customer communication was shut down");
        }
        return Optional.empty();
    }

    public EmployeeType getType() {
        return type;
    }

    private long waitingTime() {
        return ((long) (Math.random() * (maxTime - minTime)) + minTime) * 1000;
    }

}
