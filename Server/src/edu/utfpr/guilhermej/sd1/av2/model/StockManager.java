package edu.utfpr.guilhermej.sd1.av2.model;

import edu.utfpr.guilhermej.sd1.av2.services.IStockOrderMatcher;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Classe implementa as principais funções para gerenciamento de ações e lançamento de eventos relacionados à ações
 */
public class StockManager {
    private IStockOrderMatcher matcher;

    private final Map<String, List<StockOrder>> buyOrders;
    private final Map<String, List<StockOrder>> sellOrders;
    private final List<StockOrder> allOrders;

    private final Map<String, Thread> quotationMonitoringMap;

    private final Map<Consumer<StockEvent>, Consumer<StockEvent>> stockEventListener;

    /**
     * Constroi um gerenciador vazio
     */
    public StockManager(){
        buyOrders = new HashMap<>();
        sellOrders = new HashMap<>();
        allOrders = new ArrayList<>();

        quotationMonitoringMap = new HashMap<>();

        stockEventListener = new HashMap<>();
    }

    /**
     * Retorna o combinador de ordens usado pela classe para verificar se duas ordens são compatíveis
     * @return combinador de ordens compatíveis
     */
    public IStockOrderMatcher getMatcher() {
        return matcher;
    }

    /**
     * Define o combinador de ordens usado pela classe para verificar se duas ordens são compatíveis
     * @param matcher combinador de ordens compatíveis
     * @return o próprio objeto para construção encadeada
     */
    public StockManager setMatcher(IStockOrderMatcher matcher) {
        this.matcher = matcher;
        return this;
    }

    /**
     * Inscreve um consumidor de callback para ser chamado no lançamento de um evento
     * @param listener consumidor inscrito para callback
     */
    public void addListener(Consumer<StockEvent> listener) {
        synchronized (stockEventListener) {
            stockEventListener.put(listener, listener::accept);
        }
    }

    /**
     * Inscreve um consumidor de callback para ser chamado no lançamento de um evento,
     * aplicando-se antes um filtro sobre o evento para determinar se será chamado
     * @param listener consumidor inscrito para callback
     * @param filter predicado que filtra eventos pertinentes ao inscrito
     */
    public void addListener(Consumer<StockEvent> listener, Predicate<StockEvent> filter) {
        synchronized (stockEventListener) {
            stockEventListener.put(listener, event -> {
                if (filter.test(event))
                    listener.accept(event);
            });
        }
    }

    /**
     * Remove um consumidor de callback inscrito do lançamento de eventos
     * @param listener consumidor para se remover
     */
    public void removeListener(Consumer<StockEvent> listener){
        synchronized (stockEventListener) {
            stockEventListener.remove(listener);
        }
    }

    /**
     * Adiciona uma nova ordem de compra ou venda de ações
     * @param stockOrder ordem de compra ou venda de ações
     */
    public void addOrder(StockOrder stockOrder) {
        if(stockOrder == null)
            return;
        Stocks s = stockOrder.getStocks();
        if(s == null)
            return;
        String e = s.getEnterprise();
        if(e == null)
            return;

        //Determina a lista de ordens que será utilizada para se verificar ordens compatíveis com a adicionada
        Map<String, List<StockOrder>> matchingMap = null;
        if(stockOrder.isBuying())
            matchingMap = sellOrders;
        else if(stockOrder.isSelling())
            matchingMap = buyOrders;
        List<StockOrder> matchingList = null;
        if(matchingMap != null)
            matchingList = matchingMap.get(e);

        Stocks t;   //Ação que será transacionada
        //Enquanto houver ordens compatíveis e enquanto houver ações para realizar transação,
        //procure por possível transação
        do {
            t = null;
            StockOrder matched = null;
            if (matchingList != null) {
                for (StockOrder iter : matchingList) {
                    //Tentativa de combinar duas ordens compatíveis
                    t = matcher.matchOrders(stockOrder, iter);
                    //Se for possivel realizar uma transação, então t nao é nulo
                    if (t != null) {
                        matched = iter;
                        break;
                    }
                }
            }
            if (t != null) {
                StockOrder prev = matched.clone();
                Stocks m = matched.getStocks();
                //Diminui de cada ação a quantidade transacionada
                s.setQuantity(s.getQuantity() - t.getQuantity());
                m.setQuantity(m.getQuantity() - t.getQuantity());
                //Se a quantidade da ação encontrada durante combinação chegar a 0,
                //remove ordem da lista e lança evento de remoção
                if (m.getQuantity() == 0) {
                    removeOrder(matched, matchingMap);
                    allOrders.remove(matched);
                    launchRemovedStockOrderEvent(matched);
                } else
                //Caso contrário, apenas notifica atualização de valor
                    launchUpdatedStockOrderEvent(matched, prev);

                //Envia evento de transação realizada
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
                launchTradedStockOrderEvent(bought, sold, t);
            }
        }while (s.getQuantity() > 0 && t != null);

        //Caso sobre ações para adicionar depois de se tentar realizar as transações,
        //adicione ordem à lista e lance evento de ordem adicionada
        if(s.getQuantity() > 0){
            if (stockOrder.isBuying())
                addOrder(stockOrder, buyOrders);
            if (stockOrder.isSelling())
                addOrder(stockOrder, sellOrders);
            allOrders.add(stockOrder);
            launchAddedStockOrderEvent(stockOrder);
        }
    }

    /**
     * Inicia monitoramento de cotação de ações da empresa desejada, ou seja,
     * inicia lançamento eventos relacionados à sua cotação
     * @param enterprise nome da empresa para monitorar cotação
     */
    public void startMonitoring(String enterprise){
        Thread monitoring = null;
        synchronized (quotationMonitoringMap) {
            if (!quotationMonitoringMap.containsKey(enterprise)) {
                //Inicia thread em laço continuo, que lança a cada intervalo aleatório de tempo novo valor de cotação
                monitoring = new Thread(() -> {
                    Random random = new Random();
                    //Inicia cotação e valor anterior
                    StockQuotation quotation = new StockQuotation()
                            .setEnterprise(enterprise)
                            .setPrice(((double) random.nextInt(10000)) / 100);
                    StockQuotation previous = new StockQuotation()
                            .setEnterprise(enterprise)
                            .setPrice(quotation.getPrice());
                    //Enquanto a empresa não for retirada do mapa de threads ativas, continue execução
                    while (quotationMonitoringMap.containsKey(enterprise)) {
                        try {
                            //Lança novo evento de cotação com valor aleatório
                            launchQuotationStockOrderEvent(quotation.setPrice(randomFluctuation(random, quotation.getPrice())), previous);
                            previous.setPrice(quotation.getPrice());
                            Thread.sleep(random.nextInt(1000) + 500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
                quotationMonitoringMap.put(enterprise, monitoring);
            }
            if(monitoring != null) {
                monitoring.setName(enterprise + "StockMonitor");
                //Inicia thread de monitoração de cotação
                monitoring.start();
            }
        }
    }

    /**
     * Para o monitoramento de cotação de ações da empresa desejada, ou seja,
     * impede novo lançamento eventos relacionados à sua cotação
     * @param enterprise nome da empresa para parar monitoraramento de cotação
     */
    public void stopMonitoring(String enterprise){
        synchronized (quotationMonitoringMap) {
            if (quotationMonitoringMap.containsKey(enterprise))
                quotationMonitoringMap.remove(enterprise);
        }
    }

    /**
     * Simula flutuação de preços de ações
     * @param random fornecedor de valor aleatórios
     * @param price preço anterior à flutuação
     * @return valor de preço aleatorizado
     */
    private double randomFluctuation(Random random, Double price) {
        return Math.abs(price + random.nextGaussian()*0.2*price + random.nextGaussian());
    }

    /**
     * Lança evento de ações transacionada
     * @param bought ordem de compra relacionada à transação
     * @param sold ordem de venda relacionada à transação
     * @param tradedStock ações transacionadas entre os insersores das ordens
     */
    private void launchTradedStockOrderEvent(StockOrder bought, StockOrder sold, Stocks tradedStock) {
        synchronized (stockEventListener) {
            stockEventListener.values().parallelStream().forEach(v -> v.accept(StockEvent.createTradedStockOrderEvent(bought, sold, tradedStock, this)));
        }
    }

    /**
     * Lança evento de ordem de ação atualizada
     * @param newOrder ordem após alteração
     * @param prevOrder ordem antes da alteração
     */
    private void launchUpdatedStockOrderEvent(StockOrder newOrder, StockOrder prevOrder) {
        synchronized (stockEventListener) {
            stockEventListener.values().parallelStream().forEach(v -> v.accept(StockEvent.createUpdatedStockOrderEvent(prevOrder, newOrder, this)));
        }
    }

    /**
     * Lança evento de ordem de ação removida
     * @param stockOrder ordem removida
     */
    private void launchRemovedStockOrderEvent(StockOrder stockOrder) {
        synchronized (stockEventListener) {
            stockEventListener.values().parallelStream().forEach(v -> v.accept(StockEvent.createRemovedStockOrderEvent(stockOrder, this)));
        }
    }

    /**
     * Lança evento de ordem de ação adicionada
     * @param stockOrder ordem adicionada
     */
    private void launchAddedStockOrderEvent(StockOrder stockOrder) {
        synchronized (stockEventListener) {
            stockEventListener.values().parallelStream().forEach(v -> v.accept(StockEvent.createAddedStockOrderEvent(stockOrder, this)));
        }
    }

    /**
     * Lança evento de valor de cotação atualizado
     * @param quotation cotação atual da ação
     * @param previous valor anterior de cotação
     */
    private void launchQuotationStockOrderEvent(StockQuotation quotation, StockQuotation previous) {
        synchronized (stockEventListener) {
            stockEventListener.values().parallelStream().forEach(v -> v.accept(StockEvent.createQuotationStockOrderEvent(quotation, previous)));
        }
    }

    /**
     * Adiciona ordem a um mapa de lista de ordens correspondente
     * @param stockOrder ordem para adição
     * @param ordersMap mapa de ordens para adição
     */
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

    /**
     * Remove ordem de um mapa de lista de ordens correspondente
     * @param stockOrder ordem para remoção
     * @param ordersMap mapa de ordens para remoção
     */
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
