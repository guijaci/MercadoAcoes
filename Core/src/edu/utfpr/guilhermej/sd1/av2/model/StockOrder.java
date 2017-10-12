package edu.utfpr.guilhermej.sd1.av2.model;

import java.io.Serializable;

/**
 * Ordem de ação, aplicações padrões são ordens de compra e de venda
 */
public abstract class StockOrder implements Serializable, Comparable<StockOrder>{
    protected Long version = 0L;
    protected Long id = 0L;
    private Stockholder orderPlacer;
    private Stocks stocks;

    private static Long idCount = 0L;

    /**
     * Constrói uma nova ordem de ação, associada à um id
     */
    public StockOrder() {
        id = produceId();
    }

    /**
     * Constroi uma copia de uma ordem de ação, copiando tambem as ações associadas (profunda),
     * mas não o acionista associado (rasa)
     * @param stockOrder ordem para realizar copia
     */
    public StockOrder(StockOrder stockOrder){
        id = stockOrder.getId();
        version = stockOrder.getVersion();
        orderPlacer = stockOrder.getOrderPlacer();
        stocks = new Stocks(stockOrder.getStocks());
    }

    /**
     * Retorna novo identificador para ordem
     * @return identificador da ordem
     */
    private static synchronized Long produceId(){
        return idCount++;
    }

    /**
     * Verifica se uma ordem pode ser combinada com esta
     * @param other ordem para verificar compatibilidade
     * @return true se as ordens são compatíveis, false caso contrário
     */
    public abstract boolean matchOrder(StockOrder other);


    /**
     * Retorna true se é uma ordem de compra
     * @return true se é uma ordem de compra
     */
    public abstract boolean isBuying();

    /**
     * Retorna true se é uma ordem de venda
     * @return true se é uma ordem de venda
     */
    public abstract boolean isSelling();

    /**
     * Realiza a copia desta ordem
     * @return uma copia desta ordem
     */
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
