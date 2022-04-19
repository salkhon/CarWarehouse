package com.carwarehouse.server;

import com.carwarehouse.server.controller.ServerStatsController;
import com.carwarehouse.server.model.ServerDatabase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMain extends Application {
    ServerStatsController serverStatsController;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("view//server-stats.fxml"));
        Parent root = fxmlLoader.load();
        this.serverStatsController = fxmlLoader.getController();
        this.serverStatsController.setStage(stage);
        stage.setScene(new Scene(root, 900, 650));
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        this.serverStatsController.close();
        ServerDatabase.overwriteData();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
