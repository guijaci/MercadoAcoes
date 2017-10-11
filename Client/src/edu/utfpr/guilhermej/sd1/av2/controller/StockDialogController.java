package edu.utfpr.guilhermej.sd1.av2.controller;

import edu.utfpr.guilhermej.sd1.av2.model.Stocks;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;

public class StockDialogController {
    public TextField enterpriseTextField;
    public Spinner<Double> priceSpinner;
    public Spinner<Integer> quantitySpinner;
    private Parent form;

    public Dialog<Stocks> getDialog(String title){
        Dialog<Stocks> dialog = new Dialog<>();
        dialog.setTitle(title);
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(form);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(button ->
                button.equals(ButtonType.OK)?
                        new Stocks()
                                .setEnterprise(enterpriseTextField.getText())
                                .setQuantity(quantitySpinner.getValue().longValue())
                                .setPrice(priceSpinner.getValue()) :
                        null);

        Node okButton = dialogPane.lookupButton(ButtonType.OK);
        StringProperty enterpriseProperty = enterpriseTextField.textProperty();
        okButton.setDisable(enterpriseProperty.getValue().isEmpty());
        enterpriseProperty.addListener((observable, oldValue, newValue) ->
                okButton.setDisable(newValue.isEmpty()));

        return dialog;
    }

    public void setStock(Stocks stock) {
        if(stock != null){
            enterpriseTextField.textProperty().setValue(stock.getEnterprise());
            priceSpinner.getValueFactory().setValue(stock.getPrice());
            quantitySpinner.getValueFactory().setValue(stock.getQuantity().intValue());
        }
    }

    private void initializeFields() {
        SpinnerValueFactory<Double> doubleValueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.01D, 1000000D, 1D, 100D);
        doubleValueFactory.setConverter(new StringConverter<Double>() {
            private final DecimalFormat df = new DecimalFormat("'$'0.00");
            private final DecimalFormat df_alt = new DecimalFormat("#.##");
            @Override
            public String toString(Double value) {
                if (value == null)
                    return "";
                return df.format(value);
            }

            @Override
            public Double fromString(String value) {
                if (value == null)
                    return 0D;
                value = value.trim();
                if (value.length() < 1)
                    return 0D;
                try {
                    return df.parse(value).doubleValue();
                } catch (ParseException ex) {
                    try {
                        return df_alt.parse(value).doubleValue();
                    } catch (ParseException e) {
                        return 0D;
                    }
                }
            }
        });
        priceSpinner.setValueFactory(doubleValueFactory);

        SpinnerValueFactory<Integer> integerValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000000, 1, 100);
        quantitySpinner.setValueFactory(integerValueFactory);
    }

    public static StockDialogController loadNewWindow(){
        try {
            FXMLLoader loader = new FXMLLoader(StockDialogController.class.getResource("../../../../../../res/view/stockOrderDialog.fxml"));
            Parent form = loader.load();
            StockDialogController controller = loader.getController();
            controller.form = form;
            controller.initializeFields();
            return controller;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
