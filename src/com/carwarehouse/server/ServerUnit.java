package com.carwarehouse.server;

import com.carwarehouse.Car;
import com.carwarehouse.Protocol;
import com.carwarehouse.server.model.ServerDatabase;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class ServerUnit implements Runnable {
    private final Socket socket;

    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;


    public ServerUnit(Socket socket) throws IOException {
        this.socket = socket;
        this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
        this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
    }

    public void close() throws IOException {
        this.objectOutputStream.close();
        this.objectInputStream.close();
        this.socket.close();
    }

    public void interrupt() {
        Thread.currentThread().interrupt();
    }

    @Override
    public void run() {
        String clientRequest = "";
        while (!clientRequest.equals(Protocol.LOGOUT) && !this.socket.isClosed()) {
            try {
                clientRequest = this.objectInputStream.readUTF();

                if (clientRequest.startsWith(Protocol.LOGIN)) {
                    try {
                        handleLogin(clientRequest);
                    } catch (SQLException sqlException) {
                        System.out.println("COULD NOT HANDLE LOGIN: " + sqlException.getMessage());
                    }
                } else if (clientRequest.startsWith(Protocol.GET)) {
                    try {
                        handleGetRequest(clientRequest);
                    } catch (IOException ioException1) {
                        System.out.println("COULD NOT HANDLE GET REQUEST: " + ioException1.getMessage());
                    }
                } else if (clientRequest.startsWith(Protocol.ADD)) {
                    try {
                        handleAddRequest();
                    } catch (ClassNotFoundException classNotFoundException) {
                        System.out.println("COULD NOT ADD CAR: " + classNotFoundException);
                    }
                } else if (clientRequest.startsWith(Protocol.DELETE)) {
                    try {
                        handleDeleteRequest(clientRequest);
                    } catch (IOException ioException1) {
                        System.out.println("COULD NOT DELETE CAR: " + ioException1.getMessage());
                    }
                } else if (clientRequest.startsWith(Protocol.EDIT)) {
                    try {
                        handleEditRequest();
                    } catch (ClassNotFoundException classNotFoundException) {
                        System.out.println("COULD NOT EDIT CAR: " + classNotFoundException.getMessage());
                    }
                } else if (clientRequest.startsWith(Protocol.BUY_CAR)) {
                    try {
                        handleBuyRequest(clientRequest);
                    } catch (IOException ioException1) {
                        System.out.println("COULD NOT BUY CAR: " + ioException1.getMessage());
                    }
                }
            } catch (IOException ioException) {
                System.out.println("COULD NOT READ CLIENT REQUEST: " + ioException.getMessage());
            }
        }

        try {
            close();
        } catch (IOException ioException) {
            System.out.println("COULD NOT CLOSE SERVER UNIT AFTER LOGOUT");
        }
    }

    public void handleLogin(String clientRequest) throws SQLException, IOException {
        String[] splits = clientRequest.split(Protocol.DELIM);
        String username = splits[1];
        String password = "";
        if (splits.length >= 3) {
            password = splits[2];
        }

        System.out.println(username + " " + password);
        String serverResponse;
        boolean match = ServerDatabase.verifyUsernameAndPassword(username, password);
        if (match) {
            serverResponse = Protocol.LOGIN_SUCCESSFUL;
        } else {
            serverResponse = Protocol.LOGIN_FAILURE;
        }
        this.objectOutputStream.writeUTF(serverResponse);
        this.objectOutputStream.flush();
    }

    public void handleGetRequest(String request) throws IOException {
        if (request.equalsIgnoreCase(Protocol.GET_ALL_CARS)) {
            handleGetAllCarsRequest();
        }
    }

    public void handleGetAllCarsRequest() throws IOException {
        this.objectOutputStream.reset();
        this.objectOutputStream.writeUnshared(ServerDatabase.getAllCars());
        this.objectOutputStream.flush();
    }

    public void handleAddRequest() throws IOException, ClassNotFoundException {
        Car car = (Car) this.objectInputStream.readObject();
        String response = ServerDatabase.addCar(car);
        this.objectOutputStream.writeUTF(response);
        this.objectOutputStream.flush();
    }

    private void handleDeleteRequest(String clientRequest) throws IOException {
        String[] splits = clientRequest.split(Protocol.DELIM);
        String registrationNum = splits[1];
        String response = ServerDatabase.deleteCar(registrationNum);
        this.objectOutputStream.writeUTF(response);
        this.objectOutputStream.flush();
    }

    public void handleEditRequest() throws IOException, ClassNotFoundException {
        Car editedCar = (Car) this.objectInputStream.readObject();
        String response = ServerDatabase.editCar(editedCar);
        this.objectOutputStream.writeUTF(response);
        this.objectOutputStream.flush();
    }

    public void handleBuyRequest(String clientRequest) throws IOException {
        String[] split = clientRequest.split(Protocol.DELIM);
        this.objectOutputStream.writeUTF(ServerDatabase.buyCar(split[1]));
        this.objectOutputStream.flush();
    }
}
