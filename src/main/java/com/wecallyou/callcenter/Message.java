package com.wecallyou.callcenter;

public class Message {
    private int order;

    public Message(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return order == message.order;
    }

    @Override
    public int hashCode() {
        return order;
    }
}
