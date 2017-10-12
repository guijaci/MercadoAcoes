package edu.utfpr.guilhermej.sd1.av2.controller;

import edu.utfpr.guilhermej.sd1.av2.model.*;
import edu.utfpr.guilhermej.sd1.av2.util.CellStockOrderProperties;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador principal da interface gráfica do cliente
 */
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
    private Dialog<ITransactionRoomListener> eventDialog = null;

    private ObservableList<StockOrder> orderList;
    private Map<Object, CellStockOrderProperties> stockOrderPropertyMap = new HashMap<>();

    private ObservableList<String> eventList;

    /**
     * Método de inicialização do controlador. Invocada pelo framework do JavaFX
     */
    @FXML
    void initialize() {
        //inicializa e exibe caixa de dialogo de registro de acionistas
        TextInputDialog dialog = createRegistrationDialog();
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent())
            currentUser = new Stockholder().setName(result.get().trim());
        else
            Platform.exit();
        nameLabel.setText(currentUser.getName());

        //Inicializa lista de objetos observaveis (pelos objetos da UI)
        orderList = FXCollections.observableArrayList();
        eventList = FXCollections.observableArrayList();
        //Registra transformadores de valores de parametros de ordens de ação para as celulas da tabela.
        //Se baseia em CellStockOrderProperties e num mapa de associação de ordens para recuperar valores observaveis
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

    /**
     * Metodo fabrica para caixa de dialogo de registro de acionista
     * @return caixa de dialogo de registro de acionista criado
     */
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

    /**
     * Callback de botão "Buy Stocks" pressionado
     * @param actionEvent evento lançado ao pressionar botão
     */
    public void onBuyStocks(ActionEvent actionEvent) {
        //Carrega caixa de dialogo de criação de ações
        if(stockDialogController == null)
            stockDialogController = StockDialogController.load();
        stockDialogController.setStock(lastStocks);
        //Exibe caixa de dialogo de criação de ações
        Dialog<Stocks> stockDialog = stockDialogController.createDialog("Buy Stocks");
        stockDialog.showAndWait().ifPresent(stockToBuy -> {
            try {
                lastStocks = stockToBuy;
                //Envia solicitação de compra de ações à sala de transações
                StockOrder stockOrder = transactionRoom.createBuyOrder(currentUser, stockToBuy);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Callback de botão "Sell Stocks" pressionado
     * @param actionEvent evento lançado ao pressionar botão
     */
    public void onSellStocks(ActionEvent actionEvent) {
        //Carrega caixa de dialogo de criação de ações
        if(stockDialogController == null)
            stockDialogController = StockDialogController.load();
        stockDialogController.setStock(lastStocks);
        //Exibe caixa de dialogo de criação de ações
        Dialog<Stocks> stockDialog = stockDialogController.createDialog("Sell Stocks");
        Optional<Stocks> response = stockDialog.showAndWait();
        response.ifPresent(stockToSell -> {
            try {
                lastStocks = stockToSell;
                //Envia solicitação de venda de ações à sala de transações
                StockOrder stockOrder = transactionRoom.createSellOrder(currentUser, stockToSell);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Callback de botão "Create Subscription" pressionado
     * @param actionEvent evento lançado ao pressionar botão
     */
    public void onCreateSubscription(ActionEvent actionEvent) {
        //Carrega e inicializa caixa de dialogo para assinatura de eventos
        if(eventDialog == null)
            eventDialog = EventDialogController.load()
                    //Filtro e Callback para eventos de ordem compra de ação
                    .setBuyEventFilter(event -> event.getEventType() == StockEvent.StockEventType.ADDED && event.getNewOrder().isBuying())
                    .setBuyEventListener(event -> Platform.runLater(() ->
                            eventList.add(getBuyEventDescription(event))))
                    //Filtro e Callback para eventos de ordem venda de ação
                    .setSellEventFilter(event -> event.getEventType() == StockEvent.StockEventType.ADDED && event.getNewOrder().isSelling())
                    .setSellEventListener(event -> Platform.runLater(() ->
                            eventList.add(getSellEventDescription(event))))
                    //Filtro e Callback para eventos de transação de ação
                    .setTradeEventFilter(event -> event.getEventType() == StockEvent.StockEventType.TRADED)
                    .setTradeEventListener(event -> Platform.runLater(() ->
                            eventList.add(getTradeEventDescription(event))))
                    .createDialog("Create Subscription");
        //O tipo de assinante de eventos que será criado depende da seleção do usuário
        eventDialog.showAndWait().ifPresent(eventListener -> {
            try {
                //Adiciona assinante de eventos criado à sala de transações de ações
                transactionRoom.addListener(eventListener);
                //Exibe dialogo de confirmação
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Subscription created");
                alert.setHeaderText(null);
                alert.setContentText("You have subscribed succesfully!");

                alert.showAndWait();
            } catch (RemoteException e) {
                //Exibe dialogo de falha na assinatura
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Subscription not created");
                alert.setHeaderText(null);
                alert.setContentText("You haven't subscribed!");
                alert.show();
                e.printStackTrace();
            }
        });
    }

    /**
     * Callback de botão "Remove Subscription" pressionado
     * @param actionEvent evento lançado ao pressionar botão
     */
    public void onRemoveSubscription(ActionEvent actionEvent) {
        throw new NotImplementedException();
    }

    /**
     * Callback de botão "Stock Quotation" pressionado
     * @param actionEvent evento lançado ao pressionar botão
     */
    public void onStartQuotation(ActionEvent actionEvent) {
        //Exibe caixa de diálogo para recuperar nome da empresa para monitorar cotação
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Stock StockQuotation");
        dialog.setHeaderText("To view the quotation, enter the enterprise name.");
        dialog.setContentText("Enterprise:");
        dialog.getEditor().setPromptText("Enter company name...");

        dialog.showAndWait().ifPresent(name -> {
            //Exibe janela de monitor de cotação
            StockQuotationController stockQuotationController = StockQuotationController
                    .load(name)
                    .setTransactionRoom(transactionRoom);
            Stage stage = stockQuotationController.createStage();
            if(stage != null) {
                stage.show();
                stage.requestFocus();
            }
        });
    }

    /**
     * Cria um desccrição de evento de ordem de compra de ações para exibir para o usuário
     * @param event evento de ordem de compra de ações  para ser descrito
     * @return descrição do evento de ordem de compra de ações
     */
    private String getBuyEventDescription(StockEvent event) {
        return String.format("%s wants to buy %d %s stocks for $%01.02f",
        event.getNewOrder().getOrderPlacer().getName(),
        event.getNewOrder().getStocks().getQuantity(),
        event.getNewOrder().getStocks().getEnterprise(),
        event.getNewOrder().getStocks().getPrice());
    }

    /**
     * Cria um desccrição de evento de ordem de venda de ações para exibir para o usuário
     * @param event evento de ordem de venda de ações  para ser descrito
     * @return descrição do evento de ordem de venda de ações
     */
    private String getSellEventDescription(StockEvent event) {
        return String.format("%s wants to sell %d %s stocks for $%01.02f",
                event.getNewOrder().getOrderPlacer().getName(),
                event.getNewOrder().getStocks().getQuantity(),
                event.getNewOrder().getStocks().getEnterprise(),
                event.getNewOrder().getStocks().getPrice());
    }

    /**
     * Cria um desccrição de evento de transação de ações para exibir para o usuário
     * @param event evento de transação de ações  para ser descrito
     * @return descrição do evento de transação de ações
     */
    private String getTradeEventDescription(StockEvent event) {
        return String.format("%s bought from %s %d %s stocks for $%01.02f",
                event.getBuyOrder().getOrderPlacer().getName(),
                event.getSellOrder().getOrderPlacer().getName(),
                event.getTradedStock().getQuantity(),
                event.getTradedStock().getEnterprise(),
                event.getTradedStock().getPrice());
    }

    /**
     * Registra sala de transações de ações interna e inicializa ela de acordo
     * @param transactionRoom sala de transações de ações para registro
     * @return Este objeto para construção encadeada
     */
    @Override
    public TransactionRoomController setTransactionRoom(ITransactionRoom transactionRoom) {
        super.setTransactionRoom(transactionRoom);
        try {
            //Cria assinante de eventos relacionados à este acionista
            TransactionRoomStockEventListener serverStockEventListener = new TransactionRoomStockEventListener()
                    .setListener(stockEvent -> Platform.runLater( () -> {
                        System.out.println("Event: " + stockEvent.getEventType());
                        switch (stockEvent.getEventType()){
                        //Adiciona item em tabela caso ordem tenha sido adicionada
                        case ADDED:
                            StockOrder addedOrder = stockEvent.getNewOrder();
                            stockOrderPropertyMap.put(addedOrder.getId(), setProperties(new CellStockOrderProperties(), addedOrder));
                            orderList.add(addedOrder);
                            //Exibe evento em lista de eventos
                            if(addedOrder.isBuying())
                                eventList.add(getBuyEventDescription(stockEvent));
                            if(addedOrder.isSelling())
                                eventList.add(getSellEventDescription(stockEvent));
                            break;
                        //Remove item em tabela caso ordem tenha sido removida
                        case REMOVED:
                            StockOrder removedOrder = stockEvent.getPreviousOrder();
                            stockOrderPropertyMap.remove(removedOrder.getId());
                            orderList.remove(removedOrder);
                            break;
                        //Atualiza item em tabela caso ordem tenha sido modificada
                        case UPDATED:
                            StockOrder newValue = stockEvent.getNewOrder();
                            if(stockOrderPropertyMap.containsKey(newValue.getId()))
                                setProperties(stockOrderPropertyMap.get(newValue.getId()), newValue);
                            break;
                        //Exibe evento em lista de eventos
                        case TRADED:
                            eventList.add(getTradeEventDescription(stockEvent));
                            break;
                        }
                    }))
                    //Filtra eventos por acionista
                    .setFilter(stockEvent -> stockEvent.isParticipant(currentUser));
            transactionRoom.addListener(serverStockEventListener);
        } catch (RemoteException e) {
            e.printStackTrace();
            Platform.exit();
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
