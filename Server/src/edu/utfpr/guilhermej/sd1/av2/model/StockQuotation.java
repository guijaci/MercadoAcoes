package edu.utfpr.guilhermej.sd1.av2.model;

import java.io.Serializable;

public class StockQuotation implements Serializable{
    private String enterprise;
    private Double price;

    public String getEnterprise(){
        return enterprise;
    }

    public StockQuotation setEnterprise(String enterprise) {
        this.enterprise = enterprise;
        return this;
    }

    public Double getPrice() {
        return price;
    }

    public StockQuotation setPrice(Double price) {
        this.price = price;
        return this;
    }
}
