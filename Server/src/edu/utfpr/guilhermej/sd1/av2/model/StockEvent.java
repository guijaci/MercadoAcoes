package edu.utfpr.guilhermej.sd1.av2.model;

import java.io.Serializable;

public class StockEvent implements Serializable{
    private Object observable;
    private StockEventType eventType;
    private StockOrder newOrder;
    private StockOrder previousOrder;
    private StockOrder buyOrder;
    private StockOrder sellOrder;
    private Stocks tradedStock;
    private StockQuotation newQuotation;
    private StockQuotation previousQuotation;

    protected StockEvent(){
        observable = null;
        eventType = null;
        previousOrder = null;
        newOrder = null;
        buyOrder = null;
        sellOrder = null;
        tradedStock = null;
        newQuotation = null;
    }

    public static StockEvent createAddedStockOrderEvent(StockOrder stockOrder, Object triggerer){
        return new StockEvent()
            .setEventType(StockEventType.ADDED)
            .setNewOrder(stockOrder)
            .setObservable(triggerer);
    }

    public static StockEvent createRemovedStockOrderEvent(StockOrder stockOrder, Object triggerer){
        return new StockEvent()
                .setEventType(StockEventType.REMOVED)
                .setPreviousOrder(stockOrder)
                .setObservable(triggerer);
    }

    public static StockEvent createUpdatedStockOrderEvent(StockOrder previousValue, StockOrder newValue, Object triggerer){
        return new StockEvent()
                .setEventType(StockEventType.UPDATED)
                .setPreviousOrder(previousValue)
                .setNewOrder(newValue)
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

    public static StockEvent createQuotationStockOrderEvent(StockQuotation quotation, StockQuotation previous){
        return new StockEvent()
                .setEventType(StockEventType.QUOTATION)
                .setNewQuotation(quotation)
                .setPreviousQuotation(previous);
    }
    
    public boolean isParticipant(Stockholder holder){
        if(holder == null)
            return false;
        switch(eventType) {
            case ADDED:
                return getNewOrder() != null &&
                        holder.equals(getNewOrder().getOrderPlacer());
            case REMOVED:
                return getPreviousOrder() != null &&
                        holder.equals(getPreviousOrder().getOrderPlacer());
            case TRADED:
                return (getBuyOrder() != null &&
                        holder.equals(getBuyOrder().getOrderPlacer())) ||
                        (getSellOrder() != null &&
                        holder.equals(getSellOrder().getOrderPlacer()));
            case UPDATED:
                return getNewOrder() != null &&
                        holder.equals(getNewOrder().getOrderPlacer());
            case QUOTATION:
                return false;
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
                return  getNewOrder() != null &&
                            enterprise.trim().equalsIgnoreCase(getNewOrder().getStocks().getEnterprise().trim());
            case REMOVED:
                return  getPreviousOrder() != null &&
                            enterprise.trim().equalsIgnoreCase(getPreviousOrder().getStocks().getEnterprise().trim());
            case TRADED:
                return  (getBuyOrder() != null &&
                            enterprise.trim().equalsIgnoreCase(getBuyOrder().getStocks().getEnterprise().trim())) ||
                        (getSellOrder() != null &&
                            enterprise.trim().equalsIgnoreCase(getSellOrder().getStocks().getEnterprise().trim()));
            case UPDATED:
                return  getNewOrder() != null &&
                            enterprise.trim().equalsIgnoreCase(getNewOrder().getStocks().getEnterprise().trim());
            case QUOTATION:
                return getNewQuotation() != null &&
                            enterprise.trim().equalsIgnoreCase(getNewQuotation().getEnterprise().trim());
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

    public StockOrder getPreviousOrder() {
        return previousOrder;
    }

    public StockEvent setPreviousOrder(StockOrder previousOrder) {
        this.previousOrder = previousOrder;
        return this;
    }

    public StockOrder getNewOrder() {
        return newOrder;
    }

    public StockEvent setNewOrder(StockOrder newOrder) {
        this.newOrder = newOrder;
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

    public StockQuotation getNewQuotation() {
        return newQuotation;
    }

    public StockEvent setNewQuotation(StockQuotation newQuotation) {
        this.newQuotation = newQuotation;
        return this;
    }

    public StockQuotation getPreviousQuotation() {
        return previousQuotation;
    }

    public StockEvent setPreviousQuotation(StockQuotation previousQuotation) {
        this.previousQuotation = previousQuotation;
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
