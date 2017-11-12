package com.wecallyou.callcenter.dispatchers;

import com.wecallyou.callcenter.Message;

import java.util.concurrent.ArrayBlockingQueue;

import static com.wecallyou.callcenter.dispatchers.Employee.*;

public class Dispatchers {
    private static int DEFAULT_MIN_TIME = 10;
    private static int DEFAULT_MAX_TIME = 15;

    public static DefaultDispatcher rejectCallsDispatcher(int numberOfOperators,
                                                          int numberOfSupervisors,
                                                          int numberOfDirectors) {
        return new DefaultDispatcher(operator(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfOperators,
                new DefaultDispatcher(supervisor(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfSupervisors,
                        new DefaultDispatcher(director(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfDirectors,
                                new NullDispatcher())));
    }


    public static DefaultDispatcher rejectCallsDispatcher(int numberOfOperators,
                                                          int numberOfSupervisors,
                                                          int numberOfDirectors,
                                                          int minProcessingTime,
                                                          int maxProcessingTime) {
        return new DefaultDispatcher(operator(minProcessingTime, maxProcessingTime), numberOfOperators,
                new DefaultDispatcher(supervisor(minProcessingTime, maxProcessingTime), numberOfSupervisors,
                        new DefaultDispatcher(director(minProcessingTime, maxProcessingTime), numberOfDirectors,
                                new NullDispatcher())));
    }


    public static DefaultDispatcher nonOrderGuaranteeDispatcher(int numberOfOperators,
                                                                int numberOfSupervisors,
                                                                int numberOfDirectors,
                                                                int maxQueueSize,
                                                                int minProcessingTime,
                                                                int maxProcessingTime) {
        ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(maxQueueSize);
        return new DefaultDispatcher(operator(minProcessingTime, maxProcessingTime), numberOfOperators,
                new DefaultDispatcher(supervisor(minProcessingTime, maxProcessingTime), numberOfSupervisors,
                        new DefaultDispatcher(director(minProcessingTime, maxProcessingTime), numberOfDirectors,
                                new NullDispatcher(messages), messages), messages), messages);
    }


    public static DefaultDispatcher nonOrderGuaranteeDispatcher(int numberOfOperators,
                                                                int numberOfSupervisors,
                                                                int numberOfDirectors,
                                                                int maxQueueSize) {
        ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(maxQueueSize);
        return new DefaultDispatcher(operator(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfOperators,
                new DefaultDispatcher(supervisor(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfSupervisors,
                        new DefaultDispatcher(director(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfDirectors,
                                new NullDispatcher(messages), messages), messages), messages);
    }
}
