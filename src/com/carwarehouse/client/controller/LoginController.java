package com.carwarehouse.client.controller;

import com.carwarehouse.Car;
import com.carwarehouse.Protocol;
import com.carwarehouse.client.model.network.DataTransfer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class LoginController {
    private Stage primaryStage;

    @FXML
    public RadioButton viewerToggle;
    @FXML
    public RadioButton manufacturerToggle;

    @FXML
    public TextField usernameTextField;
    @FXML
    public PasswordField passwordField;

    @FXML
    public Button enterButton;

    @FXML
    public Label invalidLoginLabel;

    private BooleanProperty usernameEmpty;

    public void initialize() {
        this.invalidLoginLabel.setVisible(false);
        this.passwordField.disableProperty().bind(this.viewerToggle.selectedProperty());
        this.viewerToggle.setSelected(true);

        this.usernameEmpty = new SimpleBooleanProperty(true);
        this.usernameTextField.setOnKeyReleased(keyEvent ->
                LoginController.this.usernameEmpty.set(LoginController.this.usernameTextField.getText().isEmpty()));

        this.enterButton.disableProperty().bind(this.usernameEmpty);
    }

    public void setStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    public void enterButtonHandle(ActionEvent actionEvent) {
        String username = this.usernameTextField.getText();
        String password = this.passwordField.getText();
        String type = this.viewerToggle.isSelected() ? Protocol.TYPE_VIEWER : Protocol.TYPE_MANUFACTURER;
        System.out.println(username + " " + password + " " + type);
        try {
            if (DataTransfer.getInstance().verifyLogin(username, password)) {
                this.invalidLoginLabel.setVisible(false);
                try {
                    setUpViewerWindow();
                } catch (IOException ioException1) {
                    ioException1.printStackTrace();
                    System.out.println("COULD NOT SET UP VIEWER WINDOW: " + ioException1.getMessage());
                }
            } else {
                this.invalidLoginLabel.setVisible(true);
            }
        } catch (IOException ioException) {
            System.out.println("COULD NOT VERIFY USERNAME-PASSWORD");
            System.out.println(ioException.getMessage());
            ioException.printStackTrace();
        }
    }

    public void setUpViewerWindow() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("..//view//viewer-window.fxml"));
        Parent root = fxmlLoader.load();
        this.primaryStage.hide();
        this.primaryStage.setScene(new Scene(root, 1200, 750));
        ViewerWindowController viewerWindowController = fxmlLoader.getController();

        List<Car> cars;
        try {
            cars = DataTransfer.getInstance().getAllCars();
        } catch (ClassNotFoundException classNotFoundException) {
            System.out.println("COULD NOT SET UP VIEWER PAGE: " + classNotFoundException.getMessage());
            this.invalidLoginLabel.setVisible(true);
            return;
        }
        viewerWindowController.setParameters(this.primaryStage, cars,
                this.viewerToggle.isSelected() ? Protocol.TYPE_VIEWER : Protocol.TYPE_MANUFACTURER);
        this.primaryStage.show();
    }
}
