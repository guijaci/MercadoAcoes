package edu.utfpr.guilhermej.sd1.av2.model;

import edu.utfpr.guilhermej.sd1.av2.services.IStockOrderMatcher;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class StockManager {
    private IStockOrderMatcher matcher;

    private final Map<String, List<StockOrder>> buyOrders;
    private final Map<String, List<StockOrder>> sellOrders;
    private final List<StockOrder> allOrders;

    private final Map<String, Thread> quotationMonitoringMap;

    private final Map<Consumer<StockEvent>, Consumer<StockEvent>> stockEventListener;

    public StockManager(){
        buyOrders = new HashMap<>();
        sellOrders = new HashMap<>();
        allOrders = new ArrayList<>();

        quotationMonitoringMap = new HashMap<>();

        stockEventListener = new HashMap<>();
    }

    public IStockOrderMatcher getMatcher() {
        return matcher;
    }

    public StockManager setMatcher(IStockOrderMatcher matcher) {
        this.matcher = matcher;
        return this;
    }

    public void addListener(Consumer<StockEvent> listener) {
        synchronized (stockEventListener) {
            stockEventListener.put(listener, listener::accept);
        }
    }

    public void addListener(Consumer<StockEvent> listener, Predicate<StockEvent> filter) {
        synchronized (stockEventListener) {
            stockEventListener.put(listener, event -> {
                if (filter.test(event))
                    listener.accept(event);
            });
        }
    }

    public void removeListener(Consumer<StockEvent> listener){
        synchronized (stockEventListener) {
            stockEventListener.remove(listener);
        }
    }

    public void addOrder(StockOrder stockOrder) {
        if(stockOrder == null)
            return;
        Stocks s = stockOrder.getStocks();
        if(s == null)
            return;
        String e = s.getEnterprise();
        if(e == null)
            return;

        Map<String, List<StockOrder>> matchingMap = null;
        if(stockOrder.isBuying())
            matchingMap = sellOrders;
        else if(stockOrder.isSelling())
            matchingMap = buyOrders;
        List<StockOrder> matchingList = null;
        if(matchingMap != null)
            matchingList = matchingMap.get(e);

        Stocks t;
        do {
            t = null;
            StockOrder matched = null;
            if (matchingList != null) {
                for (StockOrder iter : matchingList) {
                    t = matcher.matchOrders(stockOrder, iter);
                    if (t != null) {
                        matched = iter;
                        break;
                    }
                }
            }
            if (t != null) {
                StockOrder prev = matched.clone();
                Stocks m = matched.getStocks();
                s.setQuantity(s.getQuantity() - t.getQuantity());
                m.setQuantity(m.getQuantity() - t.getQuantity());
                if (m.getQuantity() == 0) {
                    removeOrder(matched, matchingMap);
                    allOrders.remove(matched);
                    launchRemovedStockEvent(matched);
                } else
                    launchUpdatedStockEvent(matched, prev);

                StockOrder bought = null;
                StockOrder sold = null;
                if(stockOrder.isBuying())
                    bought = stockOrder;
                else if(stockOrder.isSelling())
                    sold = stockOrder;
                if(matched.isBuying())
                    bought = matched;
                else if (matched.isSelling())
                    sold = matched;
                launchTradedStockEvent(bought, sold, t);
            }
        }while (s.getQuantity() > 0 && t != null);

        if(s.getQuantity() > 0){
            if (stockOrder.isBuying())
                addOrder(stockOrder, buyOrders);
            if (stockOrder.isSelling())
                addOrder(stockOrder, sellOrders);
            allOrders.add(stockOrder);
            launchAddedStockEvent(stockOrder);
        }
    }

    public void startMonitoring(String enterprise){
        synchronized (quotationMonitoringMap) {
            if (!quotationMonitoringMap.containsKey(enterprise)) {
                Thread monitoring = new Thread(() -> {
                    Random random = new Random();
                    StockQuotation quotation = new StockQuotation()
                            .setEnterprise(enterprise)
                            .setPrice(((double) random.nextInt(10000)) / 100);
                    StockQuotation previous = new StockQuotation()
                            .setEnterprise(enterprise)
                            .setPrice(quotation.getPrice());
                    while (quotationMonitoringMap.containsKey(enterprise)) {
                        try {
                            launchQuotationStockEvent(quotation.setPrice(randomFlutuation(random, quotation.getPrice())), previous);
                            previous.setPrice(quotation.getPrice());
                            Thread.sleep(random.nextInt(1000) + 500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                monitoring.setName(enterprise + "StockMonitor");
                monitoring.start();
                quotationMonitoringMap.put(enterprise, monitoring);
            }
        }
    }

    public void stopMonitoring(String enterprise){
        synchronized (quotationMonitoringMap) {
            if (quotationMonitoringMap.containsKey(enterprise))
                quotationMonitoringMap.remove(enterprise);
        }
    }

    private double randomFlutuation(Random random, Double price) {
        return Math.abs(price + random.nextGaussian()*0.2*price + random.nextGaussian());
    }

    private void launchTradedStockEvent(StockOrder bought, StockOrder sold, Stocks tradedStock) {
        synchronized (stockEventListener) {
            stockEventListener.values().parallelStream().forEach(v -> v.accept(StockEvent.createTradedStockOrderEvent(bought, sold, tradedStock, this)));
        }
    }

    private void launchUpdatedStockEvent(StockOrder newOrder, StockOrder prevOrder) {
        synchronized (stockEventListener) {
            stockEventListener.values().parallelStream().forEach(v -> v.accept(StockEvent.createUpdatedStockOrderEvent(prevOrder, newOrder, this)));
        }
    }

    private void launchRemovedStockEvent(StockOrder stockOrder) {
        synchronized (stockEventListener) {
            stockEventListener.values().parallelStream().forEach(v -> v.accept(StockEvent.createRemovedStockOrderEvent(stockOrder, this)));
        }
    }

    private void launchAddedStockEvent(StockOrder stockOrder) {
        synchronized (stockEventListener) {
            stockEventListener.values().parallelStream().forEach(v -> v.accept(StockEvent.createAddedStockOrderEvent(stockOrder, this)));
        }
    }

    private void launchQuotationStockEvent(StockQuotation quotation, StockQuotation previous) {
        synchronized (stockEventListener) {
            stockEventListener.values().parallelStream().forEach(v -> v.accept(StockEvent.createQuotationStockOrderEvent(quotation, previous)));
        }
    }

    private static void addOrder(StockOrder stockOrder, Map<String, List<StockOrder>> ordersMap) {
        String enterprise = stockOrder.getStocks().getEnterprise();
        List<StockOrder> orderList = null;
        if (!ordersMap.containsKey(enterprise)) {
            orderList = new ArrayList<>();
            ordersMap.put(enterprise, orderList);
        }
        else
            orderList = ordersMap.get(enterprise);
        orderList.add(stockOrder);
    }

    private static void removeOrder(StockOrder stockOrder, Map<String, List<StockOrder>> ordersMap) {
        String enterprise = stockOrder.getStocks().getEnterprise();
        List<StockOrder> matchingList = null;
        if(ordersMap.containsKey(enterprise)) {
            matchingList = ordersMap.get(enterprise);
            matchingList.remove(stockOrder);
            if (matchingList.isEmpty())
                ordersMap.remove(enterprise);
        }
    }
}
