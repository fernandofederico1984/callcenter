package com.wecallyou.callcenter;

import com.wecallyou.callcenter.dispatchers.Dispatcher;
import com.wecallyou.callcenter.dispatchers.Dispatchers;
import com.wecallyou.callcenter.dispatchers.MaximumMessagesReached;
import com.wecallyou.callcenter.report.MessageReport;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.wecallyou.callcenter.EmployeeType.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class NonOrderedDispatcherTest {

    private static final int MIN_PROCESSING_TIME = 2;
    private static final int MAX_PROCESSING_TIME = 4;
    private static final int NUMBER_OF_OPERATORS = 1;
    private static final int NUMBER_OF_SUPERVISORS = 1;
    private static final int NUMBER_OF_DIRECTORS = 1;
    private static final int MAX_QUEUE_SIZE = 2;

    private static Message message(int order) {
        return new Message(order);
    }

    @Test
    public void whenMaximumReachedStartQueueUp() throws Exception {
        Dispatcher dispatcher = Dispatchers.nonOrderGuaranteeDispatcher(NUMBER_OF_OPERATORS, NUMBER_OF_SUPERVISORS,
                NUMBER_OF_DIRECTORS, MAX_QUEUE_SIZE, MIN_PROCESSING_TIME, MAX_PROCESSING_TIME);

        for (int i = 0; i < 4; i++) {
            dispatcher.dispatchCall(new Message(i));
        }

        await().atMost(30, TimeUnit.SECONDS).until(() -> dispatcher.isDone());
        Map<Message, MessageReport> report = dispatcher.getReport();
        assertThat(report.get(message(0)).getEmployeeType(), is(OPERATOR));
        assertThat(report.get(message(1)).getEmployeeType(), is(SUPERVISOR));
        assertThat(report.get(message(2)).getEmployeeType(), is(DIRECTOR));
        assertThat(report.size(), is(4));
        dispatcher.shutdown(15);
    }


    @Test
    public void whenMaximumReachedQueuUpUntil2() throws Exception {
        Dispatcher dispatcher = Dispatchers.nonOrderGuaranteeDispatcher(NUMBER_OF_OPERATORS, NUMBER_OF_SUPERVISORS,
                NUMBER_OF_DIRECTORS, MAX_QUEUE_SIZE, MIN_PROCESSING_TIME, MAX_PROCESSING_TIME);
        boolean exceptionThrown = false;

        for (int i = 0; i < 6; i++) {
            try {
                dispatcher.dispatchCall(new Message(i));
            }
            catch (MaximumMessagesReached e){
                exceptionThrown = true;
            }
        }

        await().atMost(30, TimeUnit.SECONDS).until(() -> dispatcher.isDone());
        Map<Message, MessageReport> report = dispatcher.getReport();
        assertThat(report.get(message(0)).getEmployeeType(), is(OPERATOR));
        assertThat(report.get(message(1)).getEmployeeType(), is(SUPERVISOR));
        assertThat(report.get(message(2)).getEmployeeType(), is(DIRECTOR));
        assertThat(report.size(), is(5));
        assertTrue(exceptionThrown);
        dispatcher.shutdown(15);
    }


}
