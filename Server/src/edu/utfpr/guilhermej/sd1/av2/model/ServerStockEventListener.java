package edu.utfpr.guilhermej.sd1.av2.model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ServerStockEventListener extends UnicastRemoteObject implements IServerListener<StockEvent> {
    Predicate<StockEvent> filter;
    Consumer<StockEvent> listener;

    public ServerStockEventListener() throws RemoteException {
        listener = s -> {};
        filter = s -> true;
    }

    public Predicate<StockEvent> getFilter() {
        return filter;
    }

    public ServerStockEventListener setFilter(Predicate<StockEvent> filter) {
        this.filter = filter;
        return this;
    }

    public Consumer<StockEvent> getListener() {
        return listener;
    }

    public ServerStockEventListener setListener(Consumer<StockEvent> listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void accept(StockEvent event) throws RemoteException{
        listener.accept(event);
    }

    @Override
    public boolean test(StockEvent event) throws RemoteException {
        return filter.test(event);
    }
}
