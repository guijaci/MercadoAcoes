package edu.utfpr.guilhermej.sd1.av2.model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface ITransactionRoom extends Remote {
    void addListener(IServerListener<StockEvent> listener) throws RemoteException;
    void addListener(Consumer<StockEvent> listener) throws RemoteException;
    void addListener(Consumer<StockEvent> listener, Predicate<StockEvent> filter) throws RemoteException;
    StockOrder createBuyOrder(Stockholder placer, Stocks wantedStocks) throws RemoteException;
    StockOrder createSellOrder(Stockholder placer, Stocks sellingStocks) throws RemoteException;
}
