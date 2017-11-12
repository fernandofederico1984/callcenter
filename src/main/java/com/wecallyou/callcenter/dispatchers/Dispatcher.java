package com.wecallyou.callcenter.dispatchers;

import com.wecallyou.callcenter.Message;
import com.wecallyou.callcenter.dispatchers.exceptions.DispatchingMessageException;
import com.wecallyou.callcenter.report.MessageReport;

import java.util.Map;

/**
 * Message dispatcher. A message dispatcher is the entity that is capable of receiving messages and assign an employee
 * to process it.
 */
public interface Dispatcher {

    /**
     * Dispatch a message.
     *
     * @param message Message to be processed. Cannot be null.
     * @throws DispatchingMessageException In case the message cannot be dispatched.
     */
    void dispatchCall(Message message) throws DispatchingMessageException;

    void shutdown(long timeOut);

    boolean isDone();

    Map<Message, MessageReport> getReport();
}
