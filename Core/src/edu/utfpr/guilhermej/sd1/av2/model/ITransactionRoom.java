package edu.utfpr.guilhermej.sd1.av2.model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Interface para sala de transações remota. Utilizado pelo servidor para gerenciar ordens de ação
 */
public interface ITransactionRoom extends Remote {
    /**
     * Inscreve um consumidor de callback remoto para ser chamado no lançamento de um evento
     * @param listener consumidor inscrito para callback
     * @throws RemoteException em erros de conexão
     */
    void addListener(ITransactionRoomListener<StockEvent> listener) throws RemoteException;

    /**
     * Inscreve um consumidor de callback para ser chamado no lançamento de um evento
     * @param listener consumidor inscrito para callback
     * @throws RemoteException em erros de conexão
     */
    void addListener(Consumer<StockEvent> listener) throws RemoteException;

    /**
     * Inscreve um consumidor de callback para ser chamado no lançamento de um evento,
     * aplicando-se antes um filtro sobre o evento para determinar se será chamado
     * @param listener consumidor inscrito para callback
     * @param filter predicado que filtra eventos pertinentes ao inscrito
     * @throws RemoteException em erros de conexão
     */
    void addListener(Consumer<StockEvent> listener, Predicate<StockEvent> filter) throws RemoteException;

    /**
     * Inicia monitoramento de cotação de ações da empresa desejada, ou seja,
     * inicia lançamento eventos relacionados à sua cotação
     * @param enterprise nome da empresa para monitorar cotação
     * @throws RemoteException em erros de conexão
     */
    void startQuotationMonitoring(String enterprise) throws RemoteException;

    /**
     * Para o monitoramento de cotação de ações da empresa desejada, ou seja,
     * impede novo lançamento eventos relacionados à sua cotação
     * @param enterprise nome da empresa para parar monitoraramento de cotação
     * @throws RemoteException em erros de conexão
     */
    void stopQuotationMonitoring(String enterprise) throws RemoteException;

    /**
     * Cria uma ordem de compra de ações
     * @param placer acionista requerente da ordem
     * @param wantedStocks ações desejadas na compra
     * @return ordem de compra de ação criada
     * @throws RemoteException em erros de conexão
     */
    StockOrder createBuyOrder(Stockholder placer, Stocks wantedStocks) throws RemoteException;

    /**
     * Cria uma ordem de venda de ações
     * @param placer acionista requerente da ordem
     * @param sellingStocks ações sendo vendidas
     * @return ordem de venda de ação criada
     * @throws RemoteException em erros de conexão
     */
    StockOrder createSellOrder(Stockholder placer, Stocks sellingStocks) throws RemoteException;
}
