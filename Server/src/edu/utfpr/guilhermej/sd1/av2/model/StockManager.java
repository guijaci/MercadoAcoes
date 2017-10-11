package edu.utfpr.guilhermej.sd1.av2.model;

import edu.utfpr.guilhermej.sd1.av2.services.IStockOrderMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class StockManager {
    private IStockOrderMatcher matcher;

    private Map<String, List<StockOrder>> buyOrders;
    private Map<String, List<StockOrder>> sellOrders;
    private List<StockOrder> allOrders;

    private Map<Integer, Consumer<StockEvent>> stockEventListener;

    public StockManager(){
        buyOrders = new HashMap<>();
        sellOrders = new HashMap<>();
        allOrders = new ArrayList<>();

        stockEventListener = new HashMap<>();
    }

    public IStockOrderMatcher getMatcher() {
        return matcher;
    }

    public StockManager setMatcher(IStockOrderMatcher matcher) {
        this.matcher = matcher;
        return this;
    }

    void addListener(Consumer<StockEvent> listener){
        int key = listener.hashCode();
        stockEventListener.put(key, listener::accept);
    }

    void addListener(Consumer<StockEvent> listener, Predicate<StockEvent> filter){
        int key = listener.hashCode();
        stockEventListener.put(key, event -> {
            if(filter.test(event))
                listener.accept(event);
        });
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

    private void launchTradedStockEvent(StockOrder bought, StockOrder sold, Stocks tradedStock) {
        stockEventListener.forEach((k,v) -> v.accept(StockEvent.createTradedStockOrderEvent(bought, sold, tradedStock,this)));
    }

    private void launchUpdatedStockEvent(StockOrder newOrder, StockOrder prevOrder) {
        stockEventListener.forEach((k,v)-> v.accept(StockEvent.createUpdatedStockOrderEvent(prevOrder, newOrder, this)));
    }

    private void launchRemovedStockEvent(StockOrder stockOrder) {
        stockEventListener.forEach((k,v) -> v.accept(StockEvent.createRemovedStockOrderEvent(stockOrder, this)));
    }

    private void launchAddedStockEvent(StockOrder stockOrder) {
        stockEventListener.forEach((k, v) -> v.accept(StockEvent.createAddedStockOrderEvent(stockOrder, this)));
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
}
