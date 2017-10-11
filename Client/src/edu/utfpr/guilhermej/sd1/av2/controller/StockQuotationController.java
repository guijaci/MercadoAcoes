package edu.utfpr.guilhermej.sd1.av2.controller;

import edu.utfpr.guilhermej.sd1.av2.model.ITransactionRoom;
import edu.utfpr.guilhermej.sd1.av2.model.ServerStockEventListener;
import edu.utfpr.guilhermej.sd1.av2.model.StockEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

public class StockQuotationController {
    public LineChart<Number, Number> stockQuotationChart;

    XYChart.Series<Number, Number> series;
    List<Number> dataList;

    ITransactionRoom transactionRoom;
    ServerStockEventListener eventListener;

    private String enterprise;
    private Parent root;

    @FXML
    void initialize(){
        dataList = new ArrayList<>();
        for(int i = 0; i < 100; i++)
            dataList.add(0);
        series = new XYChart.Series<>();
        dataList.forEach(number -> series.getData().add(new XYChart.Data<>(series.getData().size(), number)));
        stockQuotationChart.getData().add(series);
    }

    public StockQuotationController setTransactionRoom(ITransactionRoom transactionRoom) {
        try {
            this.transactionRoom = transactionRoom;
            transactionRoom.startQuotationMonitoring(enterprise);
            eventListener = new ServerStockEventListener()
                    .setFilter(event ->
                            event.getEventType() == StockEvent.StockEventType.QUOTATION &&
                            event.isFromEnterprise(enterprise))
                    .setListener(event -> Platform.runLater( ()-> {
                        dataList.remove(0);
                        dataList.add(event.getNewQuotation().getPrice());
                        refreshChart();
                   }));
            transactionRoom.addListener(eventListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Stage getStage(){
        Stage stage = new Stage();
        stage.setTitle("Monitoring " + enterprise + " quotation");
        stage.setScene(new Scene(root));
        return stage;
    }

    private void refreshChart(){
        for(int i = 0; i < dataList.size(); i++)
            series.getData().get(i).setYValue(dataList.get(i));
    }

    public static StockQuotationController loadNewWindow(String enterprise){
        try {
            FXMLLoader loader = new FXMLLoader(StockDialogController.class.getResource("../../../../../../res/view/stockQuotation.fxml"));
            Parent root = loader.load();
            StockQuotationController controller = loader.getController();

            controller.root = root;
            controller.enterprise = enterprise;
            controller.stockQuotationChart.setTitle(enterprise);

            return controller;
            } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
