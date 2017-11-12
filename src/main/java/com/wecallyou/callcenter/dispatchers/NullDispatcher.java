package com.wecallyou.callcenter.dispatchers;

import com.wecallyou.callcenter.Message;
import com.wecallyou.callcenter.dispatchers.exceptions.DispatchingMessageException;
import com.wecallyou.callcenter.dispatchers.exceptions.MaximumMessagesReached;
import com.wecallyou.callcenter.dispatchers.exceptions.NoEmployeesAvailableException;
import com.wecallyou.callcenter.report.MessageReport;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

/**
 * Null Pattern. Dispatcher that does not dispatch any message as it has no employee available.
 */
public class NullDispatcher implements Dispatcher {

    private Optional<BlockingQueue<Message>> waitingQueue = Optional.empty();

    NullDispatcher() { }

    NullDispatcher(BlockingQueue<Message> waitingQueue) {
        this.waitingQueue = Optional.ofNullable(waitingQueue);
    }

    @Override
    public void dispatchCall(Message message) throws DispatchingMessageException {
        BlockingQueue<Message> messages = waitingQueue.orElseThrow(() -> new NoEmployeesAvailableException());
        if ( !messages.offer(message) ) {
            throw new MaximumMessagesReached();
        }
    }

    @Override
    public void shutdown(long timeOut) {
    }

    @Override
    public boolean isDone() {
        if ( waitingQueue.isPresent() ) return waitingQueue.get().isEmpty();
        return true;
    }

    @Override
    public Map<Message, MessageReport> getReport() {
        return new HashMap<>();
    }
}
