package edu.utfpr.guilhermej.sd1.av2.model;

import java.io.Serializable;

/**
 * Representação de ações, possui preço, quantidade, e empresa da relacionada
 */
public class Stocks implements Serializable{
    protected Long version = 0L;
    private Double price;
    private Long quantity;

    private String enterprise;

    /**
     * Constroi novas ações
     */
    public Stocks(){}

    /**
     * Contrutor de cópia para ações
     * @param m ações para copiar
     */
    public Stocks(Stocks m) {
        price = m.getPrice();
        quantity = m.getQuantity();
        enterprise = m.getEnterprise();
    }

    public Long getVersion() {
        return version;
    }

    public Double getPrice() {
        return price;
    }

    public Stocks setPrice(Double price) {
        synchronized (version) {
            this.price = price;
            version++;
        }
        return this;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Stocks setQuantity(Long quantity) {
        synchronized (version) {
            this.quantity = quantity;
            version++;
        }
        return this;
    }

    public String getEnterprise() {
        return enterprise;
    }

    public Stocks setEnterprise(String enterprise) {
        synchronized (version) {
            this.enterprise = enterprise;
            version++;
        }
        return this;
    }
}
