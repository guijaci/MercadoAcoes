package edu.utfpr.guilhermej.sd1.av2.model;

public class SellStockOrder extends StockOrder {

    public SellStockOrder(){super();}

    public SellStockOrder(StockOrder stockOrder) {
        super(stockOrder);
    }

    @Override
    public boolean matchOrder(StockOrder other) {
        if(other == null)
            return false;
        if(other.isSelling())
            return false;

        Stockholder buyer = other.getOrderPlacer();
        Stockholder seller = getOrderPlacer();
        if(buyer.equals(seller))
            return false;

        Stocks stocksToBuy = other.getStocks();
        Stocks stocksToSell = getStocks();
        if(!stocksToBuy.getEnterprise().equalsIgnoreCase(stocksToSell.getEnterprise()))
            return false;

        if(stocksToBuy.getPrice() < stocksToSell.getPrice())
            return false;

        return true;
    }

    @Override
    public boolean isBuying() {
        return false;
    }

    @Override
    public boolean isSelling() {
        return true;
    }

    @Override
    public StockOrder clone() {
        return new SellStockOrder(this);
    }

    @Override
    public String toString() {
        Stocks s = getStocks();
        Stockholder h = getOrderPlacer();
        return  h.getName()         +
                " orders to sell "    + s.getQuantity()   +
                " stocks from "     + s.getEnterprise() +
                " for "             + String.format("$%.02f",s.getPrice());
    }
}
