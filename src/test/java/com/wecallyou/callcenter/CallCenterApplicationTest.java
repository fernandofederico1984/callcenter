package com.wecallyou.callcenter;

import com.wecallyou.callcenter.dispatchers.DefaultDispatcher;
import com.wecallyou.callcenter.dispatchers.NoEmployeesAvailableException;
import com.wecallyou.callcenter.dispatchers.NullDispatcher;
import org.junit.Test;

import static com.wecallyou.callcenter.EmployeeType.DIRECTOR;
import static com.wecallyou.callcenter.EmployeeType.OPERATOR;
import static com.wecallyou.callcenter.EmployeeType.SUPERVISOR;
import static org.junit.Assert.*;

public class CallCenterApplicationTest {

    @Test
    public void concurrentCalls() throws NoEmployeesAvailableException {
        DefaultDispatcher dispatcher = new DefaultDispatcher(OPERATOR, 1,
                new DefaultDispatcher(SUPERVISOR, 1,
                        new DefaultDispatcher(DIRECTOR, 1, new NullDispatcher())));

        for (int i = 0; i < 3; i++) {
            dispatcher.dispatchCall(i);
        }

        dispatcher.shutdown(15);

    }

}