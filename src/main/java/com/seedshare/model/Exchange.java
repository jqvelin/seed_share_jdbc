package com.seedshare.model;

public class Exchange {
    private int id;
    private boolean transferCompleted;
    private String deliveryMethod;

    public Exchange() {
    }

    public Exchange(int id, boolean transferCompleted, String deliveryMethod) {
        this.id = id;
        this.transferCompleted = transferCompleted;
        this.deliveryMethod = deliveryMethod;
    }

    public Exchange(boolean transferCompleted, String deliveryMethod) {
        this(0, transferCompleted, deliveryMethod);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isTransferCompleted() {
        return transferCompleted;
    }

    public void setTransferCompleted(boolean transferCompleted) {
        this.transferCompleted = transferCompleted;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    @Override
    public String toString() {
        return String.format("Обмен{id=%d, completed=%s, delivery='%s'}", id, transferCompleted, deliveryMethod);
    }
}
