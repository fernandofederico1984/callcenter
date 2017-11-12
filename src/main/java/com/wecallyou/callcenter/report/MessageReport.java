package com.wecallyou.callcenter.report;

import com.wecallyou.callcenter.EmployeeType;
import com.wecallyou.callcenter.Message;

public class MessageReport {
    private Message message;
    private int time;
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
