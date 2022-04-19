package com.carwarehouse.client.model.network;

import com.carwarehouse.Car;
import com.carwarehouse.Protocol;
import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class DataTransfer implements Protocol {

    private final Socket socket;

    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;

    private static DataTransfer dataTransfer;

    static {
        try {
            dataTransfer = new DataTransfer();
        } catch  (IOException ioException) {
            System.out.println("DATA TRANSFER COULD NOT BE INITIATED");
            System.out.println(ioException.getMessage());
            Platform.exit();
        }
    }

    private DataTransfer() throws IOException {
        int port = 5000;
        this.socket = new Socket("localhost", port);

        this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(this.socket.getInputStream());
    }

    public boolean verifyLogin(String username, String password) throws IOException {
        assert username != null;
        boolean success = false;
        if (!username.isEmpty()) {
            String loginString = LOGIN + username + DELIM + password;
            this.objectOutputStream.writeUTF(loginString);
            this.objectOutputStream.flush();
            loginString = this.objectInputStream.readUTF();
            success = loginString.equals(LOGIN_SUCCESSFUL);
        }
        return success;
    }

    @SuppressWarnings("unchecked")
    public List<Car> getAllCars() throws IOException, ClassNotFoundException {
        this.objectOutputStream.writeUTF(Protocol.GET_ALL_CARS);
        this.objectOutputStream.flush();
        return (List<Car>) this.objectInputStream.readUnshared();
    }

    public String addNewCar(Car car) throws IOException {
        this.objectOutputStream.writeUTF(Protocol.ADD);
        this.objectOutputStream.flush();
        this.objectOutputStream.writeObject(car);
        this.objectOutputStream.flush();
        return this.objectInputStream.readUTF();
    }

    public String deleteCarWithRegistrationNo(String registrationNumber) throws IOException {
        this.objectOutputStream.writeUTF(Protocol.DELETE + registrationNumber);
        this.objectOutputStream.flush();
        return this.objectInputStream.readUTF();
    }

    public String editCar(Car editedCar) throws IOException {
        this.objectOutputStream.writeUTF(Protocol.EDIT);
        this.objectOutputStream.flush();
        this.objectOutputStream.writeObject(editedCar);
        this.objectOutputStream.flush();
        return this.objectInputStream.readUTF();
    }

    public String buyCar(String regNo) throws IOException {
        this.objectOutputStream.writeUTF(Protocol.BUY_CAR + regNo);
        this.objectOutputStream.flush();
        return this.objectInputStream.readUTF();
    }

    public static DataTransfer getInstance() {
        return dataTransfer;
    }

    public void logout() throws IOException {
        this.objectOutputStream.writeUTF(Protocol.LOGOUT);
        this.objectOutputStream.flush();
    }

    public void close() throws IOException {
        this.objectInputStream.close();
        this.objectOutputStream.close();
        this.socket.close();
    }
}
