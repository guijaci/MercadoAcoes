package edu.utfpr.guilhermej.sd1.av2.controller;

import edu.utfpr.guilhermej.sd1.av2.model.ITransactionRoom;
import edu.utfpr.guilhermej.sd1.av2.model.TransactionRoomStockEventListener;
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

/**
 * Controlador da janela de cotação de ações
 */
public class StockQuotationController {
    public LineChart<Number, Number> stockQuotationChart;

    private XYChart.Series<Number, Number> series;
    private List<Number> dataHistory;

    private ITransactionRoom transactionRoom;
    private TransactionRoomStockEventListener eventListener;

    private String enterprise;
    private Parent root;

    /**
     * Método de inicialização do controlador. Invocada pelo framework do JavaFX
     */
    @FXML
    void initialize(){
        //Inicializa historico de dados
        dataHistory = new ArrayList<>();
        for(int i = 0; i < 100; i++)
            dataHistory.add(0);
        //Inicializa série do gráfico
        series = new XYChart.Series<>();
        dataHistory.forEach(number -> series.getData().add(new XYChart.Data<>(series.getData().size(), number)));
        stockQuotationChart.getData().add(series);
    }

    /**
     * Registra sala de transações de ações interna e inicializa ela de acordo
     * @param transactionRoom sala de transações de ações para registro
     * @return Este objeto para construção encadeada
     */
    public StockQuotationController setTransactionRoom(ITransactionRoom transactionRoom) {
        try {
            this.transactionRoom = transactionRoom;
            //Sala de transações inicia monitoramento de cotação da empresa passada
            transactionRoom.startQuotationMonitoring(enterprise);
            //Adiciona novo assinante de eventos
            eventListener = new TransactionRoomStockEventListener()
                    .setFilter(event ->
                            //Filtra apenas eventos de cotação da empresa desejada
                            event.getEventType() == StockEvent.StockEventType.QUOTATION &&
                            event.isFromEnterprise(enterprise))
                    //Atualiza histórico de cotação a cada evento recebido e modifica o gráfico
                    .setListener(event -> Platform.runLater( ()-> {
                        dataHistory.remove(0);
                        dataHistory.add(event.getNewQuotation().getPrice());
                        refreshChart();
                   }));
            transactionRoom.addListener(eventListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * Método fábrica para palco de cotação de ações
     * @return palco de cotação de ações
     */
    public Stage createStage(){
        Stage stage = new Stage();
        stage.setTitle("Monitoring " + enterprise + " quotation");
        stage.setScene(new Scene(root));
        return stage;
    }

    /**
     * Atualiza valores do gráfico com valores armazenados do histórico
     */
    private void refreshChart(){
        for(int i = 0; i < dataHistory.size(); i++)
            series.getData().get(i).setYValue(dataHistory.get(i));
    }

    /**
     * Carrega recursos gráficos para janela de cotação de ações e retorna seu controlador
     * @param enterprise empresa para se monitorar a cotação de ações
     * @return controlador da caixa de janela criado, ou null caso haja algum erro
     */
    public static StockQuotationController load(String enterprise){
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
