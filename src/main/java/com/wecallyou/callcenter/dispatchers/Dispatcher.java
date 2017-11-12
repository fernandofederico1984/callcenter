package com.wecallyou.callcenter.dispatchers;

import com.wecallyou.callcenter.Message;
import com.wecallyou.callcenter.report.MessageReport;

import java.util.Map;

public interface Dispatcher {

    void dispatchCall(Message message) throws NoEmployeesAvailableException, MaximumMessagesReached;


    void shutdown(long timeOut);

    boolean isDone();

    Map<Message, MessageReport> getReport();
}
