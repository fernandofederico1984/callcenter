package com.wecallyou.callcenter.dispatchers;

public class NullDispatcher implements Dispatcher {


    @Override
    public void dispatchCall(int message)  throws NoEmployeesAvailableException {
        throw new NoEmployeesAvailableException();
    }

    @Override
    public void shutdown(long timeOut) {
    }
}
