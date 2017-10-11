package edu.utfpr.guilhermej.sd1.av2.controller;

import edu.utfpr.guilhermej.sd1.av2.model.ServerStockEventListener;
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

    public Dialog<ServerStockEventListener> getDialog(String title){
        Dialog<ServerStockEventListener> dialog = new Dialog<>();
        dialog.setTitle(title);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(form);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Node okButton = dialogPane.lookupButton(ButtonType.OK);
        StringProperty enterpriseProperty = enterpriseTextField.textProperty();
        okButton.setDisable(enterpriseProperty.getValue().isEmpty());
        enterpriseProperty.addListener((observable, oldValue, newValue) ->
                okButton.setDisable(newValue.isEmpty()));

        dialog.setResultConverter(button -> {
            try {
                final String enterprise = enterpriseProperty.get();
                return button.equals(ButtonType.OK) ?
                        new ServerStockEventListener()
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

    public static EventDialogController loadNewWindow(){
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

    private Consumer<StockEvent> getSelectedListener(Toggle selected) {
        if(tradedRadioButton.equals(selected))
            return tradeEventListener;
        if(sellRadioButton.equals(selected))
            return sellEventListener;
        if(buyRadioButton.equals(selected))
            return buyEventListener;
        return event -> {};
    }

    private Predicate<StockEvent> getSelectedFilter(Toggle selected) {
        if(tradedRadioButton.equals(selected))
            return tradeEventFilter;
        if(sellRadioButton.equals(selected))
            return sellEventFilter;
        if(buyRadioButton.equals(selected))
            return buyEventFilter;
        return event -> false;
    }

    public EventDialogController setTradeEventListener(Consumer<StockEvent> tradeEventListener) {
        this.tradeEventListener = tradeEventListener;
        return this;
    }

    public EventDialogController setSellEventListener(Consumer<StockEvent> sellEventListener) {
        this.sellEventListener = sellEventListener;
        return this;
    }

    public EventDialogController setBuyEventListener(Consumer<StockEvent> buyEventListener) {
        this.buyEventListener = buyEventListener;
        return this;
    }

    public EventDialogController setTradeEventFilter(Predicate<StockEvent> tradeEventFilter) {
        this.tradeEventFilter = tradeEventFilter;
        return this;
    }

    public EventDialogController setSellEventFilter(Predicate<StockEvent> sellEventFilter) {
        this.sellEventFilter = sellEventFilter;
        return this;
    }

    public EventDialogController setBuyEventFilter(Predicate<StockEvent> buyEventFilter) {
        this.buyEventFilter = buyEventFilter;
        return this;
    }
}
