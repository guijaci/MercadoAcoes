package edu.utfpr.guilhermej.sd1.av2.model;

import javax.sound.midi.Soundbank;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DefaultTransactionRoom extends UnicastRemoteObject implements ITransactionRoom {
    private StockManager manager;

    public DefaultTransactionRoom() throws RemoteException{

    }

    public StockManager getManager() {
        return manager;
    }

    public DefaultTransactionRoom setManager(StockManager manager) {
        this.manager = manager;
        return this;
    }

    @Override
    public void addListener(IServerListener<StockEvent> listener) throws RemoteException {
        manager.addListener(event -> {
            try {
                listener.accept(event.setObservable(this));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }, event -> {
            try {
                return listener.test(event.setObservable(this));
            } catch (RemoteException e) {
                System.out.printf("Remote Exception: "+e.getMessage());
            }
            return false;
        });
    }

    @Override
    public void addListener(Consumer<StockEvent> listener) throws RemoteException {
        manager.addListener(event -> listener.accept(event.setObservable(this)));
    }

    @Override
    public void addListener(Consumer<StockEvent> listener, Predicate<StockEvent> filter) throws RemoteException{
        manager.addListener(event -> listener.accept(event.setObservable(this)), filter);
    }

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
