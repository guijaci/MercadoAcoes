package edu.utfpr.guilhermej.sd1.av2.controller;

import edu.utfpr.guilhermej.sd1.av2.model.CellStockOrderProperties;
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

public class ServerUiController extends TransactionRoomController {
    public TableView stockOrdersTable;
    public TableColumn orderPlacerTableColumn;
    public TableColumn orderTableColumn;
    public TableColumn quantityTableColumn;
    public TableColumn enterpriseTableColumn;
    public TableColumn priceTableColumn;

    private ObservableList<StockOrder> orderList;
    private Map<Object, CellStockOrderProperties> stockOrderPropertyMap = new HashMap<>();

    @FXML
    void initialize(){
        orderList = FXCollections.observableArrayList();
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

    @Override
    public TransactionRoomController setTransactionRoom(ITransactionRoom transactionRoom){
        super.setTransactionRoom(transactionRoom);
        try {
            transactionRoom.addListener(
                    event -> Platform.runLater(() -> {
                        switch (event.getEventType()) {
                        case ADDED:
                            StockOrder addedOrder = event.getNewValue();
                            stockOrderPropertyMap.put(addedOrder.getId(), setProperties(new CellStockOrderProperties(), addedOrder));
                            orderList.add(addedOrder);
                            break;
                        case REMOVED:
                            StockOrder removedOrder = event.getPreviousValue();
                            stockOrderPropertyMap.remove(removedOrder.getId());
                            orderList.remove(removedOrder);
                            break;
                        case UPDATED:
                            StockOrder newValue = event.getNewValue();
                            if(stockOrderPropertyMap.containsKey(newValue.getId()))
                                setProperties(stockOrderPropertyMap.get(newValue.getId()), newValue);
                            break;
                        }
                    }));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return this;
    }

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
