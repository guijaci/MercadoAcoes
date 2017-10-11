package edu.utfpr.guilhermej.sd1.av2.controller;

import edu.utfpr.guilhermej.sd1.av2.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientUiController extends TransactionRoomController {
    public Button sellStocksButton;
    public Button buyStocksButton;

    public TableView<StockOrder> stockOrdersTable;
    public TableColumn orderTableColumn;
    public TableColumn enterpriseTableColumn;
    public TableColumn priceTableColumn;
    public TableColumn quantityTableColumn;
    public Label nameLabel;
    public ListView<String> eventListView;

    private Stockholder currentUser = null;
    private Stocks lastStocks = null;

    private StockDialogController stockDialogController = null;
    private Dialog<ServerStockEventListener> eventDialog = null;

    private ObservableList<StockOrder> orderList;
    private Map<Object, CellStockOrderProperties> stockOrderPropertyMap = new HashMap<>();

    private ObservableList<String> eventList;

    @FXML
    void initialize() {
        TextInputDialog dialog = createRegistrationDialog();
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent())
            currentUser = new Stockholder().setName(result.get().trim());
        else
            Platform.exit();
        nameLabel.setText(currentUser.getName());

        orderList = FXCollections.observableArrayList();
        eventList = FXCollections.observableArrayList();
        orderTableColumn.setCellValueFactory(param -> {
            StockOrder order = StockOrder.class.cast(TableColumn.CellDataFeatures.class.cast(param).getValue());
            if(stockOrderPropertyMap.containsKey(order.getId()))
                return stockOrderPropertyMap.get(order.getId()).orderTypeProperty();
            return null;
        });
        enterpriseTableColumn.setCellValueFactory(param -> {
            StockOrder order = StockOrder.class.cast(TableColumn.CellDataFeatures.class.cast(param).getValue());
            if(stockOrderPropertyMap.containsKey(order.getId()))
                return stockOrderPropertyMap.get(order.getId()).enterpriseProperty();
            return null;
        });
        quantityTableColumn.setCellValueFactory(param -> {
            StockOrder order = StockOrder.class.cast(TableColumn.CellDataFeatures.class.cast(param).getValue());
            if(stockOrderPropertyMap.containsKey(order.getId())) {
                return stockOrderPropertyMap.get(order.getId()).quantityProperty();
            }
            return null;
        });
        priceTableColumn.setCellValueFactory(param -> {
            StockOrder order = StockOrder.class.cast(TableColumn.CellDataFeatures.class.cast(param).getValue());
            if(stockOrderPropertyMap.containsKey(order.getId()))
                return stockOrderPropertyMap.get(order.getId()).priceProperty();
            return null;
        });

        stockOrdersTable.setItems(orderList);
        eventListView.setItems(eventList);
    }

    TextInputDialog createRegistrationDialog() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Register Stockholder");
        dialog.setHeaderText("To proceed, we need for you to register.");
        dialog.setContentText("Please, enter your name:");

        Node button = dialog.getDialogPane().lookupButton(ButtonType.OK);
        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) ->
                button.setDisable(newValue.trim().isEmpty())
        );
        button.setDisable(true);
        return dialog;
    }

    public void onBuyStocks(ActionEvent actionEvent) {
        if(stockDialogController == null)
            stockDialogController = StockDialogController.loadNewWindow();
        stockDialogController.setStock(lastStocks);
        Dialog<Stocks> stockDialog = stockDialogController.getDialog("Buy Stocks");
        stockDialog.showAndWait().ifPresent(stockToBuy -> {
            try {
                lastStocks = stockToBuy;
                StockOrder stockOrder = transactionRoom.createBuyOrder(currentUser, stockToBuy);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    public void onSellStocks(ActionEvent actionEvent) {
        if(stockDialogController == null)
            stockDialogController = StockDialogController.loadNewWindow();
        stockDialogController.setStock(lastStocks);
        Dialog<Stocks> stockDialog = stockDialogController.getDialog("Sell Stocks");
        Optional<Stocks> response = stockDialog.showAndWait();
        response.ifPresent(stockToSell -> {
            try {
                lastStocks = stockToSell;
                StockOrder stockOrder = transactionRoom.createSellOrder(currentUser, stockToSell);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    public void onCreateSubscription(ActionEvent actionEvent) {
        if(eventDialog == null)
            eventDialog = EventDialogController.loadNewWindow()
                    .setBuyEventFilter(event -> event.getEventType() == StockEvent.StockEventType.ADDED && event.getNewValue().isBuying())
                    .setBuyEventListener(event -> Platform.runLater(() ->
                            eventList.add(getBuyEventDescription(event))))
                    .setSellEventFilter(event -> event.getEventType() == StockEvent.StockEventType.ADDED && event.getNewValue().isSelling())
                    .setSellEventListener(event -> Platform.runLater(() ->
                            eventList.add(getSellEventDescription(event))))
                    .setTradeEventFilter(event -> event.getEventType() == StockEvent.StockEventType.TRADED)
                    .setTradeEventListener(event -> Platform.runLater(() ->
                            eventList.add(getTradeEventDescription(event))))
                    .getDialog("Create Subscription");
        eventDialog.showAndWait().ifPresent(eventListener -> {
            try {
                transactionRoom.addListener(eventListener);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Subscription created");
                alert.setHeaderText(null);
                alert.setContentText("You have subscribed succesfully!");

                alert.showAndWait();
            } catch (RemoteException e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Subscription not created");
                alert.setHeaderText(null);
                alert.setContentText("You haven't subscribed!");
                e.printStackTrace();
            }
        });
    }

    public void onRemoveSubscription(ActionEvent actionEvent) {

    }

    private String getBuyEventDescription(StockEvent event) {
        return String.format("%s wants to buy %d %s stocks for $%01.02f",
        event.getNewValue().getOrderPlacer().getName(),
        event.getNewValue().getStocks().getQuantity(),
        event.getNewValue().getStocks().getEnterprise(),
        event.getNewValue().getStocks().getPrice());
    }

    private String getSellEventDescription(StockEvent event) {
        return String.format("%s wants to sell %d %s stocks for $%01.02f",
                event.getNewValue().getOrderPlacer().getName(),
                event.getNewValue().getStocks().getQuantity(),
                event.getNewValue().getStocks().getEnterprise(),
                event.getNewValue().getStocks().getPrice());
    }

    private String getTradeEventDescription(StockEvent event) {
        return String.format("%s bought from %s %d %s stocks for $%01.02f",
                event.getBuyOrder().getOrderPlacer().getName(),
                event.getSellOrder().getOrderPlacer().getName(),
                event.getTradedStock().getQuantity(),
                event.getTradedStock().getEnterprise(),
                event.getTradedStock().getPrice());
    }

    @Override
    public TransactionRoomController setTransactionRoom(ITransactionRoom transactionRoom) {
        super.setTransactionRoom(transactionRoom);
        try {
            ServerStockEventListener serverStockEventListener = new ServerStockEventListener()
                    .setListener(stockEvent -> Platform.runLater( () -> {
                        System.out.println("Event: " + stockEvent.getEventType());
                        switch (stockEvent.getEventType()){
                        case ADDED:
                            StockOrder addedOrder = stockEvent.getNewValue();
                            stockOrderPropertyMap.put(addedOrder.getId(), setProperties(new CellStockOrderProperties(), addedOrder));
                            orderList.add(addedOrder);
                            if(addedOrder.isBuying())
                                eventList.add(getBuyEventDescription(stockEvent));
                            if(addedOrder.isSelling())
                                eventList.add(getSellEventDescription(stockEvent));
                            break;
                        case REMOVED:
                            StockOrder removedOrder = stockEvent.getPreviousValue();
                            stockOrderPropertyMap.remove(removedOrder.getId());
                            orderList.remove(removedOrder);
                            break;
                        case UPDATED:
                            StockOrder newValue = stockEvent.getNewValue();
                            if(stockOrderPropertyMap.containsKey(newValue.getId()))
                                setProperties(stockOrderPropertyMap.get(newValue.getId()), newValue);
                            break;
                        case TRADED:
                            eventList.add(getTradeEventDescription(stockEvent));
                            break;
                        }
                    }))
                    .setFilter(stockEvent -> stockEvent.isParticipant(currentUser));
            transactionRoom.addListener(serverStockEventListener);
        } catch (RemoteException e) {
            e.printStackTrace();
            Platform.exit();
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
