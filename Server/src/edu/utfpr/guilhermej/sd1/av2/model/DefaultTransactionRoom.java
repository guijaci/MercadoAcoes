package edu.utfpr.guilhermej.sd1.av2.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Servente da sala de transações, trata chamadas à interface remotamente
 */
public class DefaultTransactionRoom extends UnicastRemoteObject implements ITransactionRoom {
    private StockManager manager;

    /**
     * Constroi uma nova sala de transação
     * @throws RemoteException em erros de conexão
     */
    public DefaultTransactionRoom() throws RemoteException{

    }

    /**
     * Retorna o gerente de ações da sala de transações
     * @return o gerente de ações da sala de transações
     */
    public StockManager getManager() {
        return manager;
    }

    /**
     * Define o gerente de ações da sala de transações
     * @param manager gerente de transações que será utilizado durante as chamadas
     * @return o próprio objeto para construção encadeada
     */
    public DefaultTransactionRoom setManager(StockManager manager) {
        this.manager = manager;
        return this;
    }

    /**
     * Inscreve um consumidor de callback remoto para ser chamado no lançamento de um evento pelo gerente de ações
     * @param listener consumidor inscrito para callback
     * @throws RemoteException em erros de conexão
     */
    @Override
    public void addListener(ITransactionRoomListener<StockEvent> listener) throws RemoteException {
        //Callback do remoto
        Consumer<StockEvent> listenerCallback = new Consumer<StockEvent>() {
            @Override
            public void accept(StockEvent event) {
                try {
                    //Chama metodo remoto
                    listener.accept(event.setObservable(DefaultTransactionRoom.this));
                } catch (RemoteException e) {
                    //Caso exista desconexão, remove o assinante desconectado
                    System.out.printf("Remote Exception: " + e.getMessage());
                    Thread remove = new Thread(() -> manager.removeListener(this));
                    remove.start();
                }
            }
        };
        //Filtro do remoto
        Predicate<StockEvent> filterCallback = event -> {
            try {
                //Chama filtro remoto
                return listener.test(event.setObservable(DefaultTransactionRoom.this));
            } catch (RemoteException e) {
                //Caso exista desconexão, remove o assinante desconectado
                System.out.printf("Remote Exception: " + e.getMessage());
                Thread remove = new Thread(() -> manager.removeListener(listenerCallback));
                remove.start();
            }
            return false;
        };
        //Adiciona callbacks e filtro remotos à lista de assinantes
        manager.addListener(listenerCallback, filterCallback);
    }

    /**
     * Inscreve um consumidor de callback para ser chamado no lançamento de um evento pelo gerente de ações
     * @param listener consumidor inscrito para callback
     * @throws RemoteException em erros de conexão
     */
    @Override
    public void addListener(Consumer<StockEvent> listener) throws RemoteException {
        manager.addListener(event -> listener.accept(event.setObservable(this)));
    }

    /**
     * Inscreve um consumidor de callback para ser chamado no lançamento de um evento pelo gerente de ações,
     * aplicando-se antes um filtro sobre o evento para determinar se será chamado
     * @param listener consumidor inscrito para callback
     * @param filter predicado que filtra eventos pertinentes ao inscrito
     * @throws RemoteException em erros de conexão
     */
    @Override
    public void addListener(Consumer<StockEvent> listener, Predicate<StockEvent> filter) throws RemoteException{
        manager.addListener(event -> listener.accept(event.setObservable(this)), filter);
    }

    /**
     * Inicia monitoramento de cotação de ações da empresa desejada junto ao gerente de ações, ou seja,
     * inicia lançamento eventos relacionados à sua cotação
     * @param enterprise nome da empresa para monitorar cotação
     * @throws RemoteException em erros de conexão
     */
    @Override
    public void startQuotationMonitoring(String enterprise) throws RemoteException {
        manager.startMonitoring(enterprise);
    }

    /**
     * Para o monitoramento de cotação de ações da empresa desejada, ou seja,
     * impede novo lançamento eventos relacionados à sua cotação
     * @param enterprise nome da empresa para parar monitoraramento de cotação
     * @throws RemoteException em erros de conexão
     */
    @Override
    public void stopQuotationMonitoring(String enterprise) throws RemoteException{
        manager.stopMonitoring(enterprise);
    }

    /**
     * Cria uma ordem de compra de ações
     * @param placer acionista requerente da ordem
     * @param wantedStocks ações desejadas na compra
     * @return ordem de compra de ação criada
     * @throws RemoteException em erros de conexão
     */
    @Override
    public StockOrder createBuyOrder(Stockholder placer, Stocks wantedStocks) throws RemoteException{
        if(placer.getName() == null || placer.getName().isEmpty()
                || placer.getId() == null || placer.getVersion() == null)
            return null;
        if(wantedStocks.getEnterprise() == null || wantedStocks.getEnterprise().isEmpty() ||
                wantedStocks.getPrice() == null || wantedStocks.getQuantity() == null ||
                wantedStocks.getVersion() == null)
            return null;
        StockOrder stockOrder = new BuyStockOrder()
                .setStocks(wantedStocks)
                .setOrderPlacer(placer);
        manager.addOrder(stockOrder);
        return stockOrder;
    }

    /**
     * Cria uma ordem de venda de ações
     * @param placer acionista requerente da ordem
     * @param sellingStocks ações sendo vendidas
     * @return ordem de venda de ação criada
     * @throws RemoteException em erros de conexão
     */
    @Override
    public StockOrder createSellOrder(Stockholder placer, Stocks sellingStocks) throws RemoteException {
        if(placer.getName() == null || placer.getName().isEmpty()
                || placer.getId() == null || placer.getVersion() == null)
            return null;
        if(sellingStocks.getEnterprise() == null || sellingStocks.getEnterprise().isEmpty() ||
                sellingStocks.getPrice() == null || sellingStocks.getQuantity() == null ||
                sellingStocks.getVersion() == null)
            return null;
        StockOrder stockOrder = new SellStockOrder()
                .setStocks(sellingStocks)
                .setOrderPlacer(placer);
        manager.addOrder(stockOrder);
        return stockOrder;
    }
}
