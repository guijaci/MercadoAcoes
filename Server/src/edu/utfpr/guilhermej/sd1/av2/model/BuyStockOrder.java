package edu.utfpr.guilhermej.sd1.av2.model;

public class BuyStockOrder extends StockOrder {

    public BuyStockOrder(){super();}

    public BuyStockOrder(StockOrder stockOrder) {
        super(stockOrder);
    }

    @Override
    public boolean matchOrder(StockOrder other) {
        if(other == null)
            return false;
        if(other.isBuying())
            return false;

        Stockholder buyer = getOrderPlacer();
        Stockholder seller = other.getOrderPlacer();
        if(buyer.equals(seller))
            return false;

        Stocks stocksToBuy = getStocks();
        Stocks stocksToSell = other.getStocks();
        if(!stocksToBuy.getEnterprise().equalsIgnoreCase(stocksToSell.getEnterprise()))
            return false;

        if(stocksToBuy.getPrice() < stocksToSell.getPrice())
            return false;

        return true;
    }

    @Override
    public boolean isBuying() {
        return true;
    }

    @Override
    public boolean isSelling() {
        return false;
    }

    @Override
    public StockOrder clone() {
        return new BuyStockOrder(this);
    }

    @Override
    public String toString() {
        Stocks s = getStocks();
        Stockholder h = getOrderPlacer();
        return  h.getName()         +
                " orders to buy "    + s.getQuantity()   +
                " stocks from "     + s.getEnterprise() +
                " for "             + String.format("$%.02f",s.getPrice());
    }
}
