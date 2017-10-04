package edu.utfpr.guilhermej.sd1.av2.model;

public class Stock {
    private long version;
    private long price;
    private long quantity;

    private String enterprise;

    public Stock(long version, long price, long quantity, String enterprise) {
        this.version = version;
        this.price = price;
        this.quantity = quantity;
        this.enterprise = enterprise;
    }
}
