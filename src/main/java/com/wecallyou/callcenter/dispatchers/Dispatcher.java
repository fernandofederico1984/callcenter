package com.wecallyou.callcenter.dispatchers;

public interface Dispatcher {

    void dispatchCall(int message) throws NoEmployeesAvailableException;

    void shutdown(long timeOut);
}
