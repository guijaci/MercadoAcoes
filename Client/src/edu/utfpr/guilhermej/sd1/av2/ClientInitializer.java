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

public class ClientInitializer extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage){
        this.primaryStage = primaryStage;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/view/mainClientUi.fxml"));
            Parent root = loader.load();
            ClientUiController controller = loader.getController();

            Registry nameService = LocateRegistry.getRegistry("localhost", 1099);
            ITransactionRoom transactionRoom =
                    (ITransactionRoom) nameService.lookup(ITransactionRoom.class.getName() + "/DefaultRoom");

            controller.setTransactionRoom(transactionRoom);

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
