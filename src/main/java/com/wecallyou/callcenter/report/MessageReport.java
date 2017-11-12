package com.wecallyou.callcenter.report;

import com.wecallyou.callcenter.EmployeeType;
import com.wecallyou.callcenter.Message;

/**
 * Report of a processed message.
 */
public class MessageReport {
    /**
     * The message that has been processed.
     */
    private Message message;

    /**
     * Processing time.
     */
    private int time;

    /**
     * Type of the employee that has processed the message.
     */
    private EmployeeType employeeType;

    public MessageReport(Message message, int time, EmployeeType employeeType) {
        this.message = message;
        this.time = time;
        this.employeeType = employeeType;
    }

    public Message getMessage() {
        return message;
    }

    public int getTime() {
        return time;
    }

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

}
