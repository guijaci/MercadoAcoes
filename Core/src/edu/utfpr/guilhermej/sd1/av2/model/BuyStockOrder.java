package edu.utfpr.guilhermej.sd1.av2.model;

public class BuyStockOrder extends StockOrder {

    /**
     * Constrói uma nova ordem de compra, associada à um id
     */
    public BuyStockOrder(){super();}

    /**
     * Constroi uma copia de uma ordem de ação, copiando tambem as ações associadas (profunda),
     * mas não o acionista associado (rasa)
     * @param stockOrder ordem para realizar copia
     */
    public BuyStockOrder(StockOrder stockOrder) {
        super(stockOrder);
    }

    /**
     * Verifica se uma ordem pode ser combinada com esta.
     * Para ser compatível, ela deve ser uma ordem de venda,
     * deve não ter sido realizada pelo mesmo acionista, ser referente à mesma empresa
     * e ter o preço menor ou igual ao preço de compra
     * @param other ordem para verificar compatibilidade
     * @return true se as ordens são compatíveis, false caso contrário
     */
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

    /**
     * Retorna true se é uma ordem de compra
     * @return true se é uma ordem de compra
     */
    @Override
    public boolean isBuying() {
        return true;
    }

    /**
     * Retorna true se é uma ordem de venda
     * @return true se é uma ordem de venda
     */
    @Override
    public boolean isSelling() {
        return false;
    }

    /**
     * Realiza a copia desta ordem
     * @return uma copia desta ordem
     */
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
