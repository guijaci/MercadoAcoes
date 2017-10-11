package edu.utfpr.guilhermej.sd1.av2.services;

import edu.utfpr.guilhermej.sd1.av2.model.StockOrder;
import edu.utfpr.guilhermej.sd1.av2.model.Stocks;

public interface IStockOrderMatcher {
    Stocks matchOrders(StockOrder firstOrder, StockOrder secondOrder);
}
