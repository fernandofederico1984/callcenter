package com.wecallyou.callcenter;

import com.wecallyou.callcenter.dispatchers.DefaultDispatcher;
import com.wecallyou.callcenter.dispatchers.Dispatchers;
import com.wecallyou.callcenter.dispatchers.NoEmployeesAvailableException;
import com.wecallyou.callcenter.report.MessageReport;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.wecallyou.callcenter.EmployeeType.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class RejectCallsDispatcherTest {

    private static final int NUMBER_OF_OPERATORS = 1;
    private static final int NUMBER_OF_SUPERVISORS = 1;
    private static final int NUMBER_OF_DIRECTORS = 1;
    private static final int MIN_PROCESSING_TIME = 2;
    private static final int MAX_PROCESSING_TIME = 4;

    private static Message message(int order) {
        return new Message(order);
    }

    @Test
    public void whenMaximumReachedFail() throws Exception{
        DefaultDispatcher dispatcher = Dispatchers.rejectCallsDispatcher(NUMBER_OF_OPERATORS, NUMBER_OF_SUPERVISORS, NUMBER_OF_DIRECTORS,
                MIN_PROCESSING_TIME, MAX_PROCESSING_TIME);

        boolean exceptionThrown = false;

        for (int i = 0; i < 4; i++) {
            try {
                dispatcher.dispatchCall(new Message(i));
            }catch (NoEmployeesAvailableException e){
                exceptionThrown = true;
            }
        }

        await().atMost(30, TimeUnit.SECONDS).until(() -> dispatcher.isDone());
        Map<Message, MessageReport> report = dispatcher.getReport();
        assertThat(report.get(message(0)).getEmployeeType(), is(OPERATOR));
        assertThat(report.get(message(1)).getEmployeeType(), is(SUPERVISOR));
        assertThat(report.get(message(2)).getEmployeeType(), is(DIRECTOR));
        assertThat(report.size(), is(5));
        dispatcher.shutdown(15);
        assertTrue(exceptionThrown);
    }



}