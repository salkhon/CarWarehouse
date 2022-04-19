package com.carwarehouse.client;

import com.carwarehouse.client.controller.LoginController;
import com.carwarehouse.client.model.network.DataTransfer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientMain extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("view//login.fxml"));
        Parent parent = fxmlLoader.load();
        ((LoginController) fxmlLoader.getController()).setStage(stage);
        stage.setScene(new Scene(parent, 600, 350));
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        try {
            DataTransfer.getInstance().logout();
            DataTransfer.getInstance().close();
        } catch (IOException ioException) {
            System.out.println("COULD NOT CLOSE DATA CONNECTION");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
