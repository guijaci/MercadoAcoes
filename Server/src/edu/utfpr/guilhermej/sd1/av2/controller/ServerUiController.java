package edu.utfpr.guilhermej.sd1.av2.controller;

import edu.utfpr.guilhermej.sd1.av2.model.StockEvent;
import edu.utfpr.guilhermej.sd1.av2.util.CellStockOrderProperties;
import edu.utfpr.guilhermej.sd1.av2.model.ITransactionRoom;
import edu.utfpr.guilhermej.sd1.av2.model.StockOrder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador de interface gráfica do Servidor
 */
public class ServerUiController extends TransactionRoomController {
    public TableView stockOrdersTable;
    public TableColumn orderPlacerTableColumn;
    public TableColumn orderTableColumn;
    public TableColumn quantityTableColumn;
    public TableColumn enterpriseTableColumn;
    public TableColumn priceTableColumn;

    private ObservableList<StockOrder> orderList;
    private Map<Object, CellStockOrderProperties> stockOrderPropertyMap = new HashMap<>();

    /**
     * Método de inicialização do controlador. Invocada pelo framework do JavaFX
     */
    @FXML
    void initialize(){
        //Inicializa lista de objetos observaveis (para tabela de ordens de ações)
        orderList = FXCollections.observableArrayList();
        //Registra transformadores de valores de parametros de ordens de ação para as celulas da tabela.
        //Se baseia em CellStockOrderProperties e num mapa de associação de ordens para recuperar valores observaveis
        orderPlacerTableColumn.setCellValueFactory(param -> {
            StockOrder order = StockOrder.class.cast(TableColumn.CellDataFeatures.class.cast(param).getValue());
            if(stockOrderPropertyMap.containsKey(order.getId()))
                return stockOrderPropertyMap.get(order.getId()).orderPlacerProperty();
            return null;
        });
        orderTableColumn.setCellValueFactory(param -> {
            StockOrder order = StockOrder.class.cast(TableColumn.CellDataFeatures.class.cast(param).getValue());
            if(stockOrderPropertyMap.containsKey(order.getId()))
                return stockOrderPropertyMap.get(order.getId()).orderTypeProperty();
            return null;
        });
        quantityTableColumn.setCellValueFactory(param -> {
            StockOrder order = StockOrder.class.cast(TableColumn.CellDataFeatures.class.cast(param).getValue());
            if(stockOrderPropertyMap.containsKey(order.getId()))
                return stockOrderPropertyMap.get(order.getId()).quantityProperty();
            return null;
        });
        enterpriseTableColumn.setCellValueFactory(param -> {
            StockOrder order = StockOrder.class.cast(TableColumn.CellDataFeatures.class.cast(param).getValue());
            if(stockOrderPropertyMap.containsKey(order.getId()))
                return stockOrderPropertyMap.get(order.getId()).enterpriseProperty();
            return null;
        });
        priceTableColumn.setCellValueFactory(param -> {
            StockOrder order = StockOrder.class.cast(TableColumn.CellDataFeatures.class.cast(param).getValue());
            if(stockOrderPropertyMap.containsKey(order.getId()))
                return stockOrderPropertyMap.get(order.getId()).priceProperty();
            return null;
        });

        stockOrdersTable.setItems(orderList);
    }

    /**
     * Registra sala de transações de ações interna e inicializa ela de acordo
     * @param transactionRoom sala de transações de ações para registro
     * @return Este objeto para construção encadeada
     */
    @Override
    public TransactionRoomController setTransactionRoom(ITransactionRoom transactionRoom){
        super.setTransactionRoom(transactionRoom);
        try {
            //Cria assinante de eventos para atualizar interface gráfica
            transactionRoom.addListener(
                    event -> Platform.runLater(() -> {
                        switch (event.getEventType()) {
                        //Adiciona item em tabela caso ordem tenha sido adicionada
                        case ADDED:
                            StockOrder addedOrder = event.getNewOrder();
                            stockOrderPropertyMap.put(addedOrder.getId(), setProperties(new CellStockOrderProperties(), addedOrder));
                            orderList.add(addedOrder);
                            break;
                        //Remove item em tabela caso ordem tenha sido removida
                        case REMOVED:
                            StockOrder removedOrder = event.getPreviousOrder();
                            stockOrderPropertyMap.remove(removedOrder.getId());
                            orderList.remove(removedOrder);
                            break;
                        //Atualiza item em tabela caso ordem tenha sido modificada
                        case UPDATED:
                            StockOrder newValue = event.getNewOrder();
                            if(stockOrderPropertyMap.containsKey(newValue.getId()))
                                setProperties(stockOrderPropertyMap.get(newValue.getId()), newValue);
                            break;
                        }
                    }),
                    //Filtra apenas eventos de adição, remoção e atualização
                    event ->
                            event.getEventType() == StockEvent.StockEventType.ADDED ||
                            event.getEventType() == StockEvent.StockEventType.REMOVED ||
                            event.getEventType() == StockEvent.StockEventType.UPDATED );
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Atualiza objeto {@link CellStockOrderProperties} associado à ordem {@link StockOrder}
     * para exibição correta na tabela
     * @param properties propriedade de ordem associadas à linha de addedOrder da tabela
     * @param addedOrder ordem de ação atualizada
     * @return propriedade de ordem de ação atualizada
     */
    private CellStockOrderProperties setProperties(CellStockOrderProperties properties, StockOrder addedOrder) {
        return properties
                .setOrderPlacer(addedOrder.getOrderPlacer().getName())
                .setOrderType(
                        (addedOrder.isSelling() ? "SELLING" : "") +
                                (addedOrder.isSelling() && addedOrder.isBuying() ? "/" : "") +
                                (addedOrder.isBuying() ? "BUYING" : ""))
                .setEnterprise(addedOrder.getStocks().getEnterprise())
                .setQuantity(addedOrder.getStocks().getQuantity().toString())
                .setPrice(String.format("$%01.02f", addedOrder.getStocks().getPrice()));
    }
}
