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

public class ServerInitializer extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("../../../../../res/mainServerUi.fxml"));
        Parent root = loader.load();

        ITransactionRoom transactionRoom = new DefaultTransactionRoom()
                .setManager(new StockManager()
                .setMatcher(new AveragePriceMatcher()));
        Registry nameService = LocateRegistry.createRegistry(1099);
        nameService.rebind(ITransactionRoom.class.getName() + "/DefaultRoom", transactionRoom);

        ServerUiController controller = loader.getController();
        controller.setTransactionRoom(transactionRoom);

        primaryStage.setTitle("Server");
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
