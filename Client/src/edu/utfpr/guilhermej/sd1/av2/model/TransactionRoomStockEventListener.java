package edu.utfpr.guilhermej.sd1.av2.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Servente de assinante de eventos de ações lançados pela sala de transações.
 * Permite adicionar callbacks e filtros para serem executados remotamente pela sala de transações
 */
public class TransactionRoomStockEventListener extends UnicastRemoteObject implements ITransactionRoomListener<StockEvent> {
    Predicate<StockEvent> filter;
    Consumer<StockEvent> listener;

    /** Constroi novo assinante de eventos remoto, com callbacks e filtros padrão
     * @throws RemoteException em erros de conexão
     */
    public TransactionRoomStockEventListener() throws RemoteException {
        listener = s -> {};
        filter = s -> true;
    }

    /**
     * Retorna o {@link Predicate} utilizado como filtro
     * @return {@link Predicate} utilziado como filtro
     */
    public Predicate<StockEvent> getFilter() {
        return filter;
    }

    /**
     * Registra filtro de eventos de ação
     * @param filter filtro de eventos de ação para registrar neste assinante
     * @return este objeto para construção encadeada
     */
    public TransactionRoomStockEventListener setFilter(Predicate<StockEvent> filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Retorna o {@link Consumer} utilizado como callback de evento
     * @return {@link Consumer} utilizado como callback de evento
     */
    public Consumer<StockEvent> getListener() {
        return listener;
    }

    /**
     * Registra callback para ser executado quando eventos for recebido
     * @param listener callback de eventos de ação para registro neste assinante
     * @return este objeto para construção encadeada
     */
    public TransactionRoomStockEventListener setListener(Consumer<StockEvent> listener) {
        this.listener = listener;
        return this;
    }

    /**
     * Método callback do assinante
     * @param event evento recebido por publicador
     * @throws RemoteException em erros de conexão
     */
    @Override
    public void accept(StockEvent event) throws RemoteException{
        listener.accept(event);
    }

    /**
     * Filtro de eventos. Retorna true caso evento passe pelo filtro, false caso contrário
     * @param event evento recebido por publicador
     * @return true caso evento passa pelo filtro, false caso contrário
     * @throws RemoteException em erros de conexão
     */
    @Override
    public boolean test(StockEvent event) throws RemoteException {
        return filter.test(event);
    }
}
