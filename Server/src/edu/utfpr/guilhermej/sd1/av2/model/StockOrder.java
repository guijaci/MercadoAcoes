package edu.utfpr.guilhermej.sd1.av2.model;

import java.io.Serializable;

public abstract class StockOrder implements Serializable, Comparable<StockOrder>{
    protected Long version = 0L;
    protected Long id = 0L;
    private Stockholder orderPlacer;
    private Stocks stocks;

    private static Long idCount = 0L;

    public StockOrder() {
        id = produceId();
    }

    public StockOrder(StockOrder stockOrder){
        id = stockOrder.getId();
        version = stockOrder.getVersion();
        orderPlacer = stockOrder.getOrderPlacer();
        stocks = new Stocks(stockOrder.getStocks());
    }

    private static synchronized Long produceId(){
        return idCount++;
    }

    public abstract boolean matchOrder(StockOrder other);

    public abstract boolean isBuying();

    public abstract boolean isSelling();

    @Override
    public abstract StockOrder clone();

    public Long getVersion() {
        return version;
    }

    public Long getId() {
        return id;
    }

    public Stocks getStocks() {
        return stocks;
    }

    public StockOrder setStocks(Stocks stocks) {
        synchronized (version) {
            this.stocks = stocks;
            version++;
        }
        return this;
    }

    public Stockholder getOrderPlacer() {
        return orderPlacer;
    }

    public StockOrder setOrderPlacer(Stockholder orderPlacer) {
        synchronized (version) {
            this.orderPlacer = orderPlacer;
            version++;
        }
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if(!this.getClass().isInstance(obj))
            return false;
        StockOrder other = this.getClass().cast(obj);
        return getId().equals(other.getId());
    }

    @Override
    public int compareTo(StockOrder o) {
        return getId().compareTo(o.getId());
    }
}
