package com.carwarehouse.client.controller;

import com.carwarehouse.Car;
import com.carwarehouse.Protocol;
import com.carwarehouse.client.model.network.DataTransfer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ViewerWindowController {
    private Stage primaryStage;

    private String userType;

    @FXML
    private TextField registrationNumberTextField;
    @FXML
    private Button registrationNumberSearchButton;
    @FXML
    private TextField carMakeTextField;
    @FXML
    private TextField carModelTextField;
    @FXML
    private Button makeModelSearchButton;

    @FXML
    private Button viewAllCarsButton;
    @FXML
    private Button addCarButton;

    @FXML
    private ImageView carImageView;
    @FXML
    private TableView<Car> carTableView;
    @FXML
    private Button buyButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label userTypeLabel;

    private IntegerProperty selectedCarIndexIntegerProperty;

    private ObservableList<Car> cars;

    public void initialize() {
        this.carTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        this.selectedCarIndexIntegerProperty = new SimpleIntegerProperty();
        this.selectedCarIndexIntegerProperty.bind(this.carTableView.getSelectionModel().selectedIndexProperty());
        this.selectedCarIndexIntegerProperty.addListener((observableValue, number, t1)
                -> displaySelectedImage(t1.intValue()));
        this.editButton.disableProperty().bind(
                this.selectedCarIndexIntegerProperty.lessThan(0));
        this.carImageView.setFitWidth(300);
        this.carImageView.setFitHeight(150);
    }

    public void setParameters(Stage stage, List<Car> cars, String type) {
        this.primaryStage = stage;
        this.cars = FXCollections.observableList(cars);
        this.userType = type;

        this.carTableView.setItems(this.cars);

        setTypePrivileges();
    }

    public void setTypePrivileges() {
        if (this.userType.equals(Protocol.TYPE_VIEWER)) {
            this.addCarButton.setVisible(false);
            this.deleteButton.setVisible(false);
            this.editButton.setVisible(false);
            this.userTypeLabel.setText(Protocol.TYPE_VIEWER);
        } else {
            this.buyButton.setVisible(false);
            this.userTypeLabel.setText(Protocol.TYPE_MANUFACTURER);
        }
    }

    private void displaySelectedImage(int changedIndex) {
        if (changedIndex > -1) {
            this.carImageView.setImage(
                    this.carTableView.getItems().get(this.selectedCarIndexIntegerProperty.get()).getImage());
        } else {
            this.carImageView.setImage(null);
        }
    }

    @FXML
    public void handleViewAllCars() {
        try {
            this.cars = FXCollections.observableList(DataTransfer.getInstance().getAllCars());
        } catch (IOException | ClassNotFoundException exception) {
            System.out.println("COULD NOT REFRESH: " + exception.getMessage());
        }
        this.carTableView.getSelectionModel().clearSelection(); // to -1
        this.carTableView.setItems(this.cars);
        this.carTableView.refresh();
    }

    @FXML
    public void handleAddNewCar(ActionEvent actionEvent) {
        Car newCar = getNewCarFromDialog();
        String response = "";
        if (newCar != null && newCar.isValid()) {
            try {
                response = DataTransfer.getInstance().addNewCar(newCar);
            } catch (IOException ioException) {
                System.out.println("COULD NOT ADD NEW CAR: " + ioException.getMessage());
            }
        }
        showProperResponseToAdd(response);

        handleViewAllCars();
    }

    private Car getNewCarFromDialog() {
        Dialog<Car> carDialog = setUpCarInputDialog();
        Optional<Car> carOptional = carDialog.showAndWait();
        return carOptional.orElse(null);
    }

    private Dialog<Car> setUpCarInputDialog() {
        return setUpCarInputDialog(null);
    }

    private Dialog<Car> setUpCarInputDialog(Car car) {
        Dialog<Car> carDialog = new Dialog<>();

        carDialog.setTitle("Add new car");
        carDialog.setHeaderText("Enter new car specifications. \nPress okay to add car.");
        carDialog.setResizable(true);

        GridPane gridPane = new GridPane();

        Label regNoLabel = new Label("Registration No: ");
        Label yearMadeLabel = new Label("Year Made: ");
        Label quantityLabel = new Label("Quantity: ");
        Label color1Label = new Label("Color 1: ");
        Label color2Label = new Label("Color 2: ");
        Label color3Label = new Label("Color 3: ");
        Label makerLabel = new Label("Maker: ");
        Label modelLabel = new Label("Model: ");
        Label priceLabel = new Label("Price: ");
        Label imageLabel = new Label("Image: ");

        TextField regNoTextField = new TextField();
        TextField yearMadeTextField = new TextField();
        TextField quantityTextField = new TextField();
        TextField color1TextField = new TextField();
        TextField color2TextField = new TextField();
        TextField color3TextField = new TextField();
        TextField makerTextField = new TextField();
        TextField modelTextField = new TextField();
        TextField priceTextField = new TextField();
        Button imageButton = new Button("Choose an image");
        FileChooser imageFileChooser = new FileChooser();
        TextField filePathTextField = new TextField("No file selected");
        Stage stage = new Stage();
        final File[] imageFile = new File[1];

        imageButton.setOnAction(actionEvent -> {
            imageFile[0] = imageFileChooser.showOpenDialog(stage);
            if (imageFile[0] != null) {
                filePathTextField.setText(imageFile[0].getAbsolutePath());
            }
        });

        if (car != null) {
            regNoTextField.setText(car.getRegistrationNumber());
            yearMadeTextField.setText("" + car.getYearMade());
            quantityTextField.setText("" + car.getQuantity());
            color1TextField.setText(car.getColor1());
            color2TextField.setText(car.getColor2());
            color3TextField.setText(car.getColor3());
            makerTextField.setText(car.getMaker());
            modelTextField.setText(car.getModel());
            priceTextField.setText("" + car.getPrice());
            imageButton.setText("Choose a different image");
        }

        gridPane.addRow(1, regNoLabel, regNoTextField);
        gridPane.addRow(2, yearMadeLabel, yearMadeTextField);
        gridPane.addRow(3, quantityLabel, quantityTextField);
        gridPane.addRow(4, color1Label, color1TextField);
        gridPane.addRow(5, color2Label, color2TextField);
        gridPane.addRow(6, color3Label, color3TextField);
        gridPane.addRow(7, makerLabel, makerTextField);
        gridPane.addRow(8, modelLabel, modelTextField);
        gridPane.addRow(9, priceLabel, priceTextField);
        gridPane.addRow(10, imageLabel, imageButton, filePathTextField);

        carDialog.getDialogPane().setContent(gridPane);

        carDialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        carDialog.setResultConverter(buttonType -> {
            Car resultCar;
            if (buttonType == ButtonType.OK) {
                try {
                    resultCar = new Car(regNoTextField.getText(),
                            Integer.parseInt(yearMadeTextField.getText()),
                            Integer.parseInt(quantityTextField.getText()),
                            color1TextField.getText(), color2TextField.getText(), color3TextField.getText(),
                            makerTextField.getText(), modelTextField.getText(),
                            Integer.parseInt(priceTextField.getText()), imageFile[0]);
                } catch (IOException ioException) {
                    System.out.println("COULD NOT CREATE CAR FROM DIALOG: " + ioException.getMessage());
                    resultCar = null;
                }
            } else {
                resultCar = null;
            }
            return resultCar;
        });
        return carDialog;
    }

    private void showProperResponseToAdd(String response) {
        Alert alert;
        if (!response.isEmpty() && response.equals(Protocol.ADD_SUCCESS)) {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Successfully added new car");
            alert.setContentText("Provided car has been added to the server database.");
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Could not add new car");
            if (response.equals(Protocol.ADD_CAR_EXISTS)) {
                alert.setHeaderText("Car with provided registration number\n" +
                        " already exists with different fields. \nCould not add.");
            } else {
                alert.setHeaderText("An error occurred.");
            }
        }
        alert.showAndWait();
    }

    @FXML
    public void handleDeleteCar(ActionEvent actionEvent) {
        String response = "";
        try {
            response = DataTransfer.getInstance().deleteCarWithRegistrationNo(
                    this.carTableView.getItems().get(this.selectedCarIndexIntegerProperty.get()).getRegistrationNumber());
        } catch (IOException ioException) {
            System.out.println("COULD NOT DELETE CAR: " + ioException.getMessage());
        }

        showProperResponseToDelete(response);
        handleViewAllCars();
    }

    private void showProperResponseToDelete(String response) {
        Alert alert;
        if (response.equals(Protocol.DELETE_SUCCESS)) {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Successful Delete");
            alert.setHeaderText("Car was successfully deleted from server");
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Delete failure");
            alert.setHeaderText("Car could not be deleted. Might not exist");
        }
        alert.showAndWait();
    }

    @FXML
    public void handleBuyCar(ActionEvent actionEvent) {
        String response = "";
        try {
            response = DataTransfer.getInstance()
                    .buyCar(this.carTableView.getItems().get(this.selectedCarIndexIntegerProperty.get())
                            .getRegistrationNumber());
        } catch (IOException ioException) {
            System.out.println("COULD NOT BUY CAR: " + ioException.getMessage());
        }
        showProperResponseToBuy(response);
        handleViewAllCars();
    }

    private void showProperResponseToBuy(String response) {
        Alert alert;
        if (response.equals(Protocol.BUY_SUCCESS)) {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Buy Successful");
            alert.setHeaderText("You have successfully bought the car!");
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Buy Failure");
            alert.setHeaderText("Could not buy car");
        }
        alert.showAndWait();
    }

    @FXML
    public void handleEdit(ActionEvent actionEvent) {
        Car editedCar = getEditedCarFromDialog();
        String response = "";
        if (editedCar != null && editedCar.isValid()) {
            try {
                response = DataTransfer.getInstance().editCar(editedCar);
            } catch (IOException ioException) {
                System.out.println("COULD NOT EDIT CAR: " + ioException.getMessage());
                return;
            }
        }
        showProperResponseToEdit(response);
        handleViewAllCars();
    }

    private Car getEditedCarFromDialog() {
        Dialog<Car> carDialog = setUpCarInputDialog(this.carTableView.getItems()
                .get(this.selectedCarIndexIntegerProperty.get()));
        Optional<Car> carOptional = carDialog.showAndWait();
        return carOptional.orElse(null);
    }

    private void showProperResponseToEdit(String response) {
        Alert alert;
        if (response.equals(Protocol.EDIT_SUCCESS)) {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Edit Successful");
            alert.setHeaderText("Car has been edited successfully");
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Edit Failure");
            alert.setHeaderText("Could not edit car.");
            if (response.equals(Protocol.EDIT_FAIL)) {
                alert.setContentText("Might not exist");
            }
        }
        alert.showAndWait();
    }

    /*
    Refreshes list by requesting updated list from server, then filters the list
    and puts in table
     */
    public void handleSearchByMakeModel(ActionEvent actionEvent) {
        handleViewAllCars();
        String make = this.carMakeTextField.getText();
        String model = this.carModelTextField.getText().isEmpty() ?
                "any" : this.carModelTextField.getText();
        List<Car> targetCars = this.cars.stream()
                .filter(car -> car.matchesMakeModel(make, model))
                .collect(Collectors.toList());
        this.carTableView.setItems(FXCollections.observableList(targetCars));
    }

    public void handleSearchByRegNo(ActionEvent actionEvent) {
        handleViewAllCars();
        String regNo = this.registrationNumberTextField.getText();
        List<Car> targetCars = this.cars.stream()
                .filter(car -> car.getRegistrationNumber().equals(regNo))
                .collect(Collectors.toList());
        this.carTableView.setItems(FXCollections.observableList(targetCars));
    }
}
