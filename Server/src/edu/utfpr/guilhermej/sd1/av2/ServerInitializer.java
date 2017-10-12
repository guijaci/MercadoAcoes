package edu.utfpr.guilhermej.sd1.av2;

import edu.utfpr.guilhermej.sd1.av2.controller.ServerUiController;
import edu.utfpr.guilhermej.sd1.av2.model.*;
import edu.utfpr.guilhermej.sd1.av2.services.AveragePriceMatcher;
import edu.utfpr.guilhermej.sd1.av2.model.DefaultTransactionRoom;
import edu.utfpr.guilhermej.sd1.av2.model.ITransactionRoom;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Ponto de partida de execução do programa Servidor, inicializa interface gráfica e serviço de nomes
 */
public class ServerInitializer extends Application {
    /**
     * Inicializa interface gráfica e serviço de nomes. Invocado pelo framework JavaFX
     * @param primaryStage palco da janela principal iniciada
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        //Carrega janela principal de interface gráfica
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/mainServerUi.fxml"));
        Parent root = loader.load();

        //Inicializa serviço de nomes e registra sala de transações de ações registrada
        ITransactionRoom transactionRoom = new DefaultTransactionRoom()
                .setManager(new StockManager()
                    .setMatcher(new AveragePriceMatcher()));
        Registry nameService = LocateRegistry.createRegistry(1099);
        nameService.rebind(ITransactionRoom.class.getName() + "/DefaultRoom", transactionRoom);

        //Injeta sala de transações de ações no controlador da interface gráfica
        ServerUiController controller = loader.getController();
        controller.setTransactionRoom(transactionRoom);

        //Exibe janela principal
        primaryStage.setTitle("Server");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
