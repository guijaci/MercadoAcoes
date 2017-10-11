package edu.utfpr.guilhermej.sd1.av2.model;

import java.io.Serializable;

public class StockEvent implements Serializable{
    private Object observable;
    private StockEventType eventType;
    private StockOrder previousValue;
    private StockOrder newValue;
    private StockOrder buyOrder;
    private StockOrder sellOrder;
    private Stocks tradedStock;

    protected StockEvent(){
        observable = null;
        eventType = null;
        previousValue = null;
        newValue = null;
        buyOrder = null;
        sellOrder = null;
        tradedStock = null;
    }

    public static StockEvent createAddedStockOrderEvent(StockOrder stockOrder, Object triggerer){
        return new StockEvent()
            .setEventType(StockEventType.ADDED)
            .setNewValue(stockOrder)
            .setObservable(triggerer);
    }

    public static StockEvent createRemovedStockOrderEvent(StockOrder stockOrder, Object triggerer){
        return new StockEvent()
                .setEventType(StockEventType.REMOVED)
                .setPreviousValue(stockOrder)
                .setObservable(triggerer);
    }

    public static StockEvent createUpdatedStockOrderEvent(StockOrder previousValue, StockOrder newValue, Object triggerer){
        return new StockEvent()
                .setEventType(StockEventType.UPDATED)
                .setPreviousValue(previousValue)
                .setNewValue(newValue)
                .setObservable(triggerer);
    }

    public static StockEvent createTradedStockOrderEvent(StockOrder buyOrder, StockOrder sellOrder, Stocks tradedStock, Object triggerer){
        return new StockEvent()
            .setEventType(StockEventType.TRADED)
            .setBuyOrder(buyOrder)
            .setSellOrder(sellOrder)
            .setTradedStock(tradedStock)
            .setObservable(triggerer);
    }

    public boolean isParticipant(Stockholder holder){
        if(holder == null)
            return false;
        switch(eventType) {
            case ADDED:
                return getNewValue() != null &&
                        holder.equals(getNewValue().getOrderPlacer());
            case REMOVED:
                return getPreviousValue() != null &&
                        holder.equals(getPreviousValue().getOrderPlacer());
            case TRADED:
                return (getBuyOrder() != null &&
                        holder.equals(getBuyOrder().getOrderPlacer())) ||
                        (getSellOrder() != null &&
                        holder.equals(getSellOrder().getOrderPlacer()));
            case UPDATED:
                return getNewValue() != null &&
                        holder.equals(getNewValue().getOrderPlacer());
            case QUOTATION:
        }
        return false;
    }

    public boolean isFromEnterprise(String enterprise) {
        if(enterprise == null)
            return false;
        if(enterprise.isEmpty())
            return false;
        switch(eventType) {
            case ADDED:
                return  getNewValue() != null &&
                            enterprise.trim().toLowerCase().equals(getNewValue().getStocks().getEnterprise().trim().toLowerCase());
            case REMOVED:
                return  getPreviousValue() != null &&
                            enterprise.trim().toLowerCase().equals(getPreviousValue().getStocks().getEnterprise().trim().toLowerCase());
            case TRADED:
                return  (getBuyOrder() != null &&
                            enterprise.trim().toLowerCase().equals(getBuyOrder().getStocks().getEnterprise().trim().toLowerCase())) ||
                        (getSellOrder() != null &&
                            enterprise.trim().toLowerCase().equals(getSellOrder().getStocks().getEnterprise().trim().toLowerCase()));
            case UPDATED:
                return  getNewValue() != null &&
                            enterprise.trim().toLowerCase().equals(getNewValue().getStocks().getEnterprise().trim().toLowerCase());
            case QUOTATION:
        }
        return false;
    }

    public Object getObservable() {
        return observable;
    }

    public StockEvent setObservable(Object observable) {
        this.observable = observable;
        return this;
    }

    public StockEventType getEventType() {
        return eventType;
    }

    public StockEvent setEventType(StockEventType eventType) {
        this.eventType = eventType;
        return this;
    }

    public StockOrder getPreviousValue() {
        return previousValue;
    }

    public StockEvent setPreviousValue(StockOrder previousValue) {
        this.previousValue = previousValue;
        return this;
    }

    public StockOrder getNewValue() {
        return newValue;
    }

    public StockEvent setNewValue(StockOrder newValue) {
        this.newValue = newValue;
        return this;
    }

    public StockOrder getBuyOrder() {
        return buyOrder;
    }

    public StockEvent setBuyOrder(StockOrder buyOrder) {
        this.buyOrder = buyOrder;
        return this;
    }

    public StockOrder getSellOrder() {
        return sellOrder;
    }

    public StockEvent setSellOrder(StockOrder sellOrder) {
        this.sellOrder = sellOrder;
        return this;
    }

    public Stocks getTradedStock() {
        return tradedStock;
    }

    public StockEvent setTradedStock(Stocks tradedStock) {
        this.tradedStock = tradedStock;
        return this;
    }

    public enum StockEventType {
        ADDED, REMOVED, UPDATED, TRADED, QUOTATION
    }
}
