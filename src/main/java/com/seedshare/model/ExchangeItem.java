package com.seedshare.model;

public class ExchangeItem {
    private int exchangeId;
    private int seedId;
    private int packetsCount;

    public ExchangeItem() {
    }

    public ExchangeItem(int exchangeId, int seedId, int packetsCount) {
        this.exchangeId = exchangeId;
        this.seedId = seedId;
        this.packetsCount = packetsCount;
    }

    public int getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(int exchangeId) {
        this.exchangeId = exchangeId;
    }

    public int getSeedId() {
        return seedId;
    }

    public void setSeedId(int seedId) {
        this.seedId = seedId;
    }

    public int getPacketsCount() {
        return packetsCount;
    }

    public void setPacketsCount(int packetsCount) {
        this.packetsCount = packetsCount;
    }

    @Override
    public String toString() {
        return String.format("Позиция обмена{exchangeId=%d, seedId=%d, packets=%d}", exchangeId, seedId, packetsCount);
    }
}
