package edu.utfpr.guilhermej.sd1.av2.util;

import javafx.beans.property.SimpleStringProperty;

/**
 * Esta classe auxilia exibição em {@link javax.swing.text.TableView} de {@link edu.utfpr.guilhermej.sd1.av2.model.StockOrder}.
 * Ela contem propriedades observaveis (como {@link SimpleStringProperty}) que {@link javax.swing.text.TableView}
 * necessita para identificar atualização de dados
 */
public class CellStockOrderProperties {
    private SimpleStringProperty orderPlacer;
    private SimpleStringProperty orderType;
    private SimpleStringProperty quantity;
    private SimpleStringProperty enterprise;
    private SimpleStringProperty price;

    public CellStockOrderProperties(){
        orderPlacer = new SimpleStringProperty();
        orderType = new SimpleStringProperty();
        quantity = new SimpleStringProperty();
        enterprise = new SimpleStringProperty();
        price = new SimpleStringProperty();
    }

    public String getOrderPlacer() {
        return orderPlacer.get();
    }

    public SimpleStringProperty orderPlacerProperty() {
        return orderPlacer;
    }

    public String getOrderType() {
        return orderType.get();
    }

    public SimpleStringProperty orderTypeProperty() {
        return orderType;
    }

    public String getQuantity() {
        return quantity.get();
    }

    public SimpleStringProperty quantityProperty() {
        return quantity;
    }

    public String getEnterprise() {
        return enterprise.get();
    }

    public SimpleStringProperty enterpriseProperty() {
        return enterprise;
    }

    public String getPrice() {
        return price.get();
    }

    public SimpleStringProperty priceProperty() {
        return price;
    }

    public CellStockOrderProperties setOrderPlacer(String orderPlacer) {
        this.orderPlacer.set(orderPlacer);
        return this;
    }

    public CellStockOrderProperties setOrderType(String orderType) {
        this.orderType.set(orderType);
        return this;
    }

    public CellStockOrderProperties setQuantity(String quantity) {
        this.quantity.set(quantity);
        return this;
    }

    public CellStockOrderProperties setEnterprise(String enterprise) {
        this.enterprise.set(enterprise);
        return this;
    }

    public CellStockOrderProperties setPrice(String price) {
        this.price.set(price);
        return this;
    }
}
