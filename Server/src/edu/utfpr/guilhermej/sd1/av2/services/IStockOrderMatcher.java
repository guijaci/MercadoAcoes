package edu.utfpr.guilhermej.sd1.av2.services;

import edu.utfpr.guilhermej.sd1.av2.model.StockOrder;
import edu.utfpr.guilhermej.sd1.av2.model.Stocks;

/**
 * Interface para combinadores de ordens
 */
public interface IStockOrderMatcher {
    /**
     * Verifica se duas ordems de ação podem ser transacionadas
     * @param firstOrder primeira ordem
     * @param secondOrder segunda ordem
     * @return ações transacionadas na combinação, ou null caso não seja possível realizar transação
     */
    Stocks matchOrders(StockOrder firstOrder, StockOrder secondOrder);
}
