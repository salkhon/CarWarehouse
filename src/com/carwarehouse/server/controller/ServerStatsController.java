package com.carwarehouse.server.controller;

import com.carwarehouse.Protocol;
import com.carwarehouse.server.ServerDispatcher;
import com.carwarehouse.server.model.ServerDatabase;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class ServerStatsController {
    @FXML
    public TableView<String[]> accountTable;
    @FXML
    public TableColumn<String[], String> nameTableColumn;
    @FXML
    public TableColumn<String[], String> typeTableColumn;
    @FXML
    public TableColumn<String[], String> passwordTableColumn;
    @FXML
    public TextField nameTextField;
    @FXML
    public TextField passwordTextField;
    @FXML
    public RadioButton manufacturerRadioButton;
    @FXML
    public RadioButton viewerRadioButton;
    @FXML
    public Button addButton;
    @FXML
    public Label addFailureLabel;
    @FXML
    public Button accountDeleteButton;

    private Stage primaryStage;
    private Thread serverDispatcherThread;
    private ServerDispatcher serverDispatcher;

    @FXML
    public BorderPane borderPane;


    public void initialize() {
        this.serverDispatcher = new ServerDispatcher(this);
        this.serverDispatcherThread = new Thread(this.serverDispatcher);
        this.serverDispatcherThread.start();

        this.addButton.setDisable(true);
        this.passwordTextField.disableProperty().bind(this.viewerRadioButton.selectedProperty());

        this.addFailureLabel.setVisible(false);

        this.accountDeleteButton.disableProperty().bind(
                this.accountTable.getSelectionModel().selectedIndexProperty().lessThan(0));

        setUpTableView();
        refreshList();
    }

    private void setUpTableView() {
        TableColumn<String[], String> nameCol = new TableColumn<>("Name");
        TableColumn<String[], String> passWordCol = new TableColumn<>("Password");
        TableColumn<String[], String> typeCol = new TableColumn<>("Type");
        nameCol.setCellValueFactory(stringCellDataFeatures ->
                new SimpleStringProperty(stringCellDataFeatures.getValue()[0]));
        passWordCol.setCellValueFactory(stringCellDataFeatures ->
                new SimpleStringProperty(stringCellDataFeatures.getValue()[1]));
        typeCol.setCellValueFactory(stringCellDataFeatures ->
                new SimpleStringProperty(stringCellDataFeatures.getValue()[2]));
        this.accountTable.getColumns().addAll(nameCol, passWordCol, typeCol);
    }

    public void setStage(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setOnCloseRequest(windowEvent -> {
            try {
                close();
            } catch (IOException ioException) {
                System.out.println("COULD NOT CLOSE SERVER DISPATCHER");
            }
        });
    }

    public void close() throws IOException {
        this.serverDispatcher.stop();
        this.serverDispatcherThread.interrupt();
    }

    @FXML
    public void handleAdd(ActionEvent actionEvent) {
        String userName = this.nameTextField.getText();
        String password = this.passwordTextField.getText();
        String type = this.manufacturerRadioButton.isSelected() ?
                Protocol.TYPE_MANUFACTURER : Protocol.TYPE_VIEWER;
        String response = ServerDatabase.addUser(userName, password, type);
        if (response.equals(Protocol.ADD_USER_FAILURE)) {
            this.addFailureLabel.setVisible(true);
        } else {
            refreshList();
            this.accountTable.refresh();
            this.nameTextField.clear();
            this.passwordTextField.clear();
            this.manufacturerRadioButton.setSelected(true);
        }
    }

    private void refreshList() {
        List<String[]> accountList = ServerDatabase.getAccountList();
        this.accountTable.setItems(FXCollections.observableList(accountList));
    }

    @FXML
    public void handleCheckFieldsFilled() {
        this.addButton.setDisable(
                this.nameTextField.getText().isEmpty() ||
                        (!this.viewerRadioButton.isSelected() && this.passwordTextField.getText().isEmpty()));
    }

    @FXML
    public void handleAccountDelete(ActionEvent actionEvent) {
        ServerDatabase.deleteAccount(this.accountTable.getSelectionModel().getSelectedItem()[0]);
        refreshList();
    }
}
