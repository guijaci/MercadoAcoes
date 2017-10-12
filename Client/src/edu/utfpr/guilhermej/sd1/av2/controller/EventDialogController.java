package edu.utfpr.guilhermej.sd1.av2.controller;

import edu.utfpr.guilhermej.sd1.av2.model.ITransactionRoomListener;
import edu.utfpr.guilhermej.sd1.av2.model.TransactionRoomStockEventListener;
import edu.utfpr.guilhermej.sd1.av2.model.StockEvent;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Controlador da caixa de dialogo para criar novas inscrições de eventos da sala de transações
 */
public class EventDialogController {
    public TextField enterpriseTextField;
    public ToggleGroup eventTypeRadioGroup;
    public RadioButton tradedRadioButton;
    public RadioButton sellRadioButton;
    public RadioButton buyRadioButton;

    private Parent form;

    private Consumer<StockEvent> tradeEventListener = event -> {};
    private Consumer<StockEvent> sellEventListener = event -> {};
    private Consumer<StockEvent> buyEventListener = event -> {};

    private Predicate<StockEvent> tradeEventFilter = event -> false;
    private Predicate<StockEvent> sellEventFilter = event -> false;
    private Predicate<StockEvent> buyEventFilter = event -> false;

    /**
     * Método fábrica para caixa de dialogo de inscrição de eventos
     * @param title título da caixa de diálogo exibida
     * @return caixa de dialogo para inscrição de eventos.
     * Resultado retorna um {@link edu.utfpr.guilhermej.sd1.av2.model.ITransactionRoomListener}
     * para realizar inscrição em um {@link edu.utfpr.guilhermej.sd1.av2.model.ITransactionRoom}
     */
    public Dialog<ITransactionRoomListener> createDialog(String title){
        Dialog<ITransactionRoomListener> dialog = new Dialog<>();
        dialog.setTitle(title);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(form);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Node okButton = dialogPane.lookupButton(ButtonType.OK);
        StringProperty enterpriseProperty = enterpriseTextField.textProperty();
        okButton.setDisable(enterpriseProperty.getValue().isEmpty());
        enterpriseProperty.addListener((observable, oldValue, newValue) ->
                okButton.setDisable(newValue.isEmpty()));

        //Retorna um assinante que chama callbacks de eventos configurados pelo usuário
        dialog.setResultConverter(button -> {
            try {
                final String enterprise = enterpriseProperty.get();
                return button.equals(ButtonType.OK) ?
                        new TransactionRoomStockEventListener()
                                .setListener(getSelectedListener(eventTypeRadioGroup.getSelectedToggle()))
                                .setFilter(((Predicate<StockEvent>) event -> event.isFromEnterprise(enterprise))
                                        .and(getSelectedFilter(eventTypeRadioGroup.getSelectedToggle()))):
                        null;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return null;
        });
        return dialog;
    }

    /**
     * Carrega recursos gráficos para caixa de dialogo e retorna seu controlador
     * @return controlador da caixa de diálogo criado, ou null caso haja algum erro
     */
    public static EventDialogController load(){
        try {
            FXMLLoader loader = new FXMLLoader(StockDialogController.class.getResource("../../../../../../res/view/eventListenerDialog.fxml"));
            Parent form = loader.load();
            EventDialogController controller = loader.getController();
            controller.form = form;
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retorna um callback (consumidor) correspondente à opção selecionada pelo usuário
     * @param selected toggle de seleção de tipo de evento selecionado
     * @return callback correspondente ao evento desejado
     */
    private Consumer<StockEvent> getSelectedListener(Toggle selected) {
        if(tradedRadioButton.equals(selected))
            return tradeEventListener;
        if(sellRadioButton.equals(selected))
            return sellEventListener;
        if(buyRadioButton.equals(selected))
            return buyEventListener;
        return event -> {};
    }

    /**
     * Retorna um filtro (predicado) correspondente à opção selecionada pelo usuário
     * @param selected toggle de seleção de tipo de evento selecionado
     * @return filtro correspondente ao evento desejado
     */
    private Predicate<StockEvent> getSelectedFilter(Toggle selected) {
        if(tradedRadioButton.equals(selected))
            return tradeEventFilter;
        if(sellRadioButton.equals(selected))
            return sellEventFilter;
        if(buyRadioButton.equals(selected))
            return buyEventFilter;
        return event -> false;
    }

    /**
     * Registra callback para eventos de transação
     * @param tradeEventListener callback para eventos de transação
     * @return este objeto para construção encadeada
     */
    public EventDialogController setTradeEventListener(Consumer<StockEvent> tradeEventListener) {
        this.tradeEventListener = tradeEventListener;
        return this;
    }

    /**
     * Registra callback para eventos de ordem de venda de ações
     * @param sellEventListener callback para eventos de ordem de venda de ações
     * @return este objeto para construção encadeada
     */
    public EventDialogController setSellEventListener(Consumer<StockEvent> sellEventListener) {
        this.sellEventListener = sellEventListener;
        return this;
    }

    /**
     * Registra callback para eventos de ordem de compra de ações
     * @param buyEventListener callback para eventos de ordem de compra de ações
     * @return este objeto para construção encadeada
     */
    public EventDialogController setBuyEventListener(Consumer<StockEvent> buyEventListener) {
        this.buyEventListener = buyEventListener;
        return this;
    }

    /**
     * Registra filter para eventos de transação
     * @param tradeEventFilter filter para eventos de transação
     * @return este objeto para construção encadeada
     */
    public EventDialogController setTradeEventFilter(Predicate<StockEvent> tradeEventFilter) {
        this.tradeEventFilter = tradeEventFilter;
        return this;
    }

    /**
     * Registra filtro para eventos de ordem de venda de ações
     * @param sellEventFilter filtro para eventos de ordem de venda de ações
     * @return este objeto para construção encadeada
     */
    public EventDialogController setSellEventFilter(Predicate<StockEvent> sellEventFilter) {
        this.sellEventFilter = sellEventFilter;
        return this;
    }

    /**
     * Registra filtro para eventos de ordem de compra de ações
     * @param buyEventFilter filtro para eventos de ordem de compra de ações
     * @return este objeto para construção encadeada
     */
    public EventDialogController setBuyEventFilter(Predicate<StockEvent> buyEventFilter) {
        this.buyEventFilter = buyEventFilter;
        return this;
    }
}
