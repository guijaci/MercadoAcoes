package edu.utfpr.guilhermej.sd1.av2.services;

import edu.utfpr.guilhermej.sd1.av2.model.StockOrder;
import edu.utfpr.guilhermej.sd1.av2.model.Stocks;

public class AveragePriceMatcher implements IStockOrderMatcher {
    @Override
    public Stocks matchOrders(StockOrder firstOrder, StockOrder secondOrder) {
        if(firstOrder == null || secondOrder == null)
            return null;
        if (firstOrder.matchOrder(secondOrder) && secondOrder.matchOrder(firstOrder)) {
            Stocks s1 = firstOrder.getStocks();
            Stocks s2 = secondOrder.getStocks();
            if(s1 == null || s2 == null || !s1.getEnterprise().equalsIgnoreCase(s2.getEnterprise()))
                return null;
            return new Stocks()
                    .setPrice((s1.getPrice() + s2.getPrice()) / 2)
                    .setEnterprise(s1.getEnterprise())
                    .setQuantity(Long.min(s1.getQuantity(), s2.getQuantity()));
        }
        else
            return null;
    }
}
