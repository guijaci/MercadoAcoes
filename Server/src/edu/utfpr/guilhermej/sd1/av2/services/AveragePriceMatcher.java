package edu.utfpr.guilhermej.sd1.av2.services;

import edu.utfpr.guilhermej.sd1.av2.model.StockOrder;
import edu.utfpr.guilhermej.sd1.av2.model.Stocks;

/**
 * Combinador de ordens que usa o preço médio entre uma oredm de compra e venda para realizar a transação
 */
public class AveragePriceMatcher implements IStockOrderMatcher {

    /**
     * Verifica se duas ordems de ação podem ser transacionadas
     * @param firstOrder  primeira ordem
     * @param secondOrder segunda ordem
     * @return ações transacionadas na combinação, cujo preço é a média entre o preço de compra e de venda,
     * ou null caso não seja possível realizar transação
     */
    @Override
    public Stocks matchOrders(StockOrder firstOrder, StockOrder secondOrder) {
        if(firstOrder == null || secondOrder == null)
            return null;
        //Verifica se ordem de venda não é aceitável para a de compra, e vice-versa
        if (firstOrder.matchOrder(secondOrder) && secondOrder.matchOrder(firstOrder)) {
            Stocks s1 = firstOrder.getStocks();
            Stocks s2 = secondOrder.getStocks();
            if(s1 == null || s2 == null || !s1.getEnterprise().equalsIgnoreCase(s2.getEnterprise()))
                return null;
            return new Stocks()
                    //O valor da transação será a media entre o preço de compra e de venda
                    .setPrice((s1.getPrice() + s2.getPrice()) / 2)
                    .setEnterprise(s1.getEnterprise())
                    //Transaciona apenas a quantidade disponível de ações entre as duas ordens
                    .setQuantity(Long.min(s1.getQuantity(), s2.getQuantity()));
        }
        else
            return null;
    }
}
