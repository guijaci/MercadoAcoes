package edu.utfpr.guilhermej.sd1.av2.model;

public class SellStockOrder extends StockOrder {

    /**
     * Constrói uma nova ordem de compra, associada à um id
     */
    public SellStockOrder(){super();}

    /**
     * Constroi uma copia de uma ordem de ação, copiando tambem as ações associadas (profunda),
     * mas não o acionista associado (rasa)
     * @param stockOrder ordem para realizar copia
     */
    public SellStockOrder(StockOrder stockOrder) {
        super(stockOrder);
    }

    /**
     * Verifica se uma ordem pode ser combinada com esta.
     * Para ser compatível, ela deve ser uma ordem de compra,
     * deve não ter sido realizada pelo mesmo acionista, ser referente à mesma empresa
     * e ter o preço maior ou igual ao preço de venda
     * @param other ordem para verificar compatibilidade
     * @return true se as ordens são compatíveis, false caso contrário
     */
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

    /**
     * Retorna true se é uma ordem de compra
     * @return true se é uma ordem de compra
     */
    @Override
    public boolean isBuying() {
        return false;
    }

    /**
     * Retorna true se é uma ordem de venda
     * @return true se é uma ordem de venda
     */
    @Override
    public boolean isSelling() {
        return true;
    }

    /**
     * Realiza a copia desta ordem
     * @return uma copia desta ordem
     */
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
