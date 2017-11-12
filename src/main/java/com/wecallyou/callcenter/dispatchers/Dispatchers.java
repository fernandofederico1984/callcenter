package com.wecallyou.callcenter.dispatchers;

import com.wecallyou.callcenter.Message;

import java.util.concurrent.ArrayBlockingQueue;

import static com.wecallyou.callcenter.Employee.*;

/**
 * Factory class to create dispatchers
 */
public class Dispatchers {
    private static int DEFAULT_MIN_TIME = 10;
    private static int DEFAULT_MAX_TIME = 15;

    /**
     * Simple implementation of a dispatcher. Fails in case there is no employee available to dispatch a message.
     * The processing time of each employee is between 10 and 15 seconds.
     *
     * @param numberOfOperators   The number of operators in the call center
     * @param numberOfSupervisors The number of supervisors in the call center
     * @param numberOfDirectors   The number of directors in the call center
     * @return a {@link Dispatcher}
     */
    public static DelegatingDispatcher rejectCallsDispatcher(int numberOfOperators,
                                                             int numberOfSupervisors,
                                                             int numberOfDirectors) {
        return new DelegatingDispatcher(operator(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfOperators,
                new DelegatingDispatcher(supervisor(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfSupervisors,
                        new DelegatingDispatcher(director(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfDirectors,
                                new NullDispatcher())));
    }


    /**
     * Simple implementation of a dispatcher. Fails in case there is no employee available to dispatch a message.
     * The processing time of each employee is between minProcessingTime and maxProcessingTime seconds.
     *
     * @param numberOfOperators   The number of operators in the call center
     * @param numberOfSupervisors The number of supervisors in the call center
     * @param numberOfDirectors   The number of directors in the call center
     * @param minProcessingTime   The minimum processing time
     * @param maxProcessingTime   The maximum processing time
     * @return a {@link Dispatcher}
     */
    public static DelegatingDispatcher rejectCallsDispatcher(int numberOfOperators,
                                                             int numberOfSupervisors,
                                                             int numberOfDirectors,
                                                             int minProcessingTime,
                                                             int maxProcessingTime) {
        return new DelegatingDispatcher(operator(minProcessingTime, maxProcessingTime), numberOfOperators,
                new DelegatingDispatcher(supervisor(minProcessingTime, maxProcessingTime), numberOfSupervisors,
                        new DelegatingDispatcher(director(minProcessingTime, maxProcessingTime), numberOfDirectors,
                                new NullDispatcher())));
    }


    /**
     * Dispatcher that does not guarantee order of messages, but guarantees delivery. In case message cannot be processed
     * it goes to a waiting state.
     *
     * @param numberOfOperators   The number of operators in the call center
     * @param numberOfSupervisors The number of supervisors in the call center
     * @param numberOfDirectors   The number of directors in the call center
     * @param maxQueueSize        maximum number of messages in the queue.
     * @param minProcessingTime   The minimum processing time
     * @param maxProcessingTime   The maximum processing time
     * @return a {@link Dispatcher}
     */
    public static Dispatcher nonOrderGuaranteeDispatcher(int numberOfOperators,
                                                         int numberOfSupervisors,
                                                         int numberOfDirectors,
                                                         int maxQueueSize,
                                                         int minProcessingTime,
                                                         int maxProcessingTime) {
        ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(maxQueueSize);
        return new GuaranteeProcessDispatcher(
                new DelegatingDispatcher(operator(minProcessingTime, maxProcessingTime), numberOfOperators,
                        new DelegatingDispatcher(supervisor(minProcessingTime, maxProcessingTime), numberOfSupervisors,
                                new DelegatingDispatcher(director(minProcessingTime, maxProcessingTime), numberOfDirectors,
                                        new NullDispatcher(messages), messages), messages), messages), messages);
    }


    /**
     * Dispatcher that does not guarantee order of messages, but guarantees delivery. In case message cannot be processed
     * it goes to a waiting state.
     *
     * @param numberOfOperators   The number of operators in the call center
     * @param numberOfSupervisors The number of supervisors in the call center
     * @param numberOfDirectors   The number of directors in the call center
     * @param maxQueueSize        maximum number of messages in the queue.
     * @return a {@link Dispatcher}
     */
    public static Dispatcher nonOrderGuaranteeDispatcher(int numberOfOperators,
                                                         int numberOfSupervisors,
                                                         int numberOfDirectors,
                                                         int maxQueueSize) {
        ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(maxQueueSize);
        return new GuaranteeProcessDispatcher(
                new DelegatingDispatcher(operator(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfOperators,
                        new DelegatingDispatcher(supervisor(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfSupervisors,
                                new DelegatingDispatcher(director(DEFAULT_MIN_TIME, DEFAULT_MAX_TIME), numberOfDirectors,
                                        new NullDispatcher(messages), messages), messages), messages), messages);
    }
}
