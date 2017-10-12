package edu.utfpr.guilhermej.sd1.av2;

import edu.utfpr.guilhermej.sd1.av2.controller.ClientUiController;
import edu.utfpr.guilhermej.sd1.av2.model.ITransactionRoom;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Ponto de partida de execução do programa Cliente, inicializa interface gráfica e serviço de nomes
 */
public class ClientInitializer extends Application {
    /**
     * Inicializa interface gráfica e serviço de nomes. Invocado pelo framework JavaFX
     * @param primaryStage palco da janela principal iniciada
     */
    @Override
    public void start(Stage primaryStage){
        try {
            //Carrega janela principal de interface gráfica
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/view/mainClientUi.fxml"));
            Parent root = loader.load();
            ClientUiController controller = loader.getController();

            //Inicializa serviço de nomes e recupera sala de transações de ações registrada
            Registry nameService = LocateRegistry.getRegistry("localhost", 1099);
            ITransactionRoom transactionRoom =
                    (ITransactionRoom) nameService.lookup(ITransactionRoom.class.getName() + "/DefaultRoom");

            //Injeta sala de transações de ações no controlador da interface gráfica
            controller.setTransactionRoom(transactionRoom);

            //Exibe janela principal
            primaryStage.setTitle("Client");
            primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.show();
        } catch (NotBoundException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
