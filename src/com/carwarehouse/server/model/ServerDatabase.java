package com.carwarehouse.server.model;

import com.carwarehouse.Car;
import com.carwarehouse.Protocol;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class ServerDatabase {
    private static final String CONNECTION_STRING = "jdbc:sqlite:D:\\Griffin_\\Java\\BUET\\CarWarehouse\\src\\com\\carwarehouse\\server\\model\\data\\cars.db";

    private static Connection connection;

    private static final String TABLE_CARS = "Cars";
    private static final String TABLE_USERS = "accounts";

    private static final String SELECT_ALL_CARS =
            "select * from " + TABLE_CARS;

    private static final String DELETE_ALL_CARS =
            "delete from " + TABLE_CARS;
    private static PreparedStatement deleteCarsPreparedStatement;

    private static final String DELETE_USERS =
            "delete from " + TABLE_USERS;
    private static PreparedStatement deleteUsersPreparedStatement;

    private static final String SELECT_ALL_USERS =
            "select * from " + TABLE_USERS;
    private static final String SELECT_USER =
            "select * from " + TABLE_USERS + " where username = ?";
    private static PreparedStatement selectUserPrepStatement;

    private static final String SELECT_CAR =
            "select * from " + TABLE_CARS + " where Registration_Number = ?";
    private static PreparedStatement selectCarByReg;

    private static final String INSERT_USER =
            "insert into " + TABLE_USERS + " values (?, ?, ?)";
    private static PreparedStatement insertUserPreparedStatement;

    private static final String INSERT_CAR =
            "insert into " + TABLE_CARS + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static PreparedStatement insertCarPreparedStatement;

    private static List<Car> carList;
    private static List<String[]> accountList;
    private static ReadWriteLock carListReadWriteLock;
    private static ReadWriteLock accountListReadWriteLock;

    static {
        try {
            connection = DriverManager.getConnection(CONNECTION_STRING);
            carList = loadAllCars();
            accountList = loadAllAccounts();
            selectUserPrepStatement = connection.prepareStatement(SELECT_USER);
            selectCarByReg = connection.prepareStatement(SELECT_CAR);
            insertUserPreparedStatement = connection.prepareStatement(INSERT_USER);
            insertCarPreparedStatement = connection.prepareStatement(INSERT_CAR);
            deleteCarsPreparedStatement = connection.prepareStatement(DELETE_ALL_CARS);
            deleteUsersPreparedStatement = connection.prepareStatement(DELETE_USERS);
        } catch (SQLException sqlException) {
            System.out.println("COULD NOT CONNECT TO DATABASE: " + sqlException.getMessage());
        }
        carListReadWriteLock = new ReentrantReadWriteLock();
        accountListReadWriteLock = new ReentrantReadWriteLock();
    }

    private static List<String[]> loadAllAccounts() throws SQLException {
        List<String[]> accountListQueried = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_USERS)) {
            while (resultSet.next()) {
                String userName = resultSet.getString(1);
                String type = resultSet.getString(3);
                String password = "";
                if (type.equals(Protocol.TYPE_MANUFACTURER)) {
                    password = resultSet.getString(2);
                }
                accountListQueried.add(new String[]{userName, password, type});
            }
        }
        return accountListQueried;
    }

    private static List<Car> loadAllCars() throws SQLException {
        List<Car> cars = new ArrayList<>();
        String regNo, col1, col2, col3, maker, model;
        int year, quantity, price;
        byte[] imageBytes;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SELECT_ALL_CARS)) {
            while (resultSet.next()) {
                regNo = resultSet.getString(1);
                year = resultSet.getInt(2);
                quantity = resultSet.getInt(3);
                col1 = resultSet.getString(4);
                col2 = resultSet.getString(5);
                if (resultSet.wasNull()) {
                    col2 = "";
                }
                col3 = resultSet.getString(6);
                if (resultSet.wasNull()) {
                    col3 = "";
                }
                maker = resultSet.getString(7);
                model = resultSet.getString(8);
                price = resultSet.getInt(9);
                imageBytes = resultSet.getBytes(10);
                if (resultSet.wasNull()) {
                    imageBytes = null;
                }
                cars.add(new Car(regNo, year, quantity, col1, col2, col3, maker, model, price, imageBytes));
            }
        }
        return cars;
    }

    public static void overwriteData() throws SQLException {
        deleteData();
        saveData();
    }

    private static void deleteData() throws SQLException {
        deleteUsersPreparedStatement.executeUpdate();
        deleteCarsPreparedStatement.executeUpdate();
    }

    private static void saveData() throws SQLException {
        for (String[] account : accountList) {
            insertAccount(account);
        }

        for (Car car : carList) {
            insertCar(car);
        }
    }

    private static void insertAccount(String[] account) throws SQLException {
        insertUserPreparedStatement.setString(1, account[0]);
        insertUserPreparedStatement.setString(2, account[1]);
        insertUserPreparedStatement.setString(3, account[2]);
        insertUserPreparedStatement.executeUpdate();
    }

    private static void insertCar(Car car) throws SQLException {
        insertCarPreparedStatement.setString(1, car.getRegistrationNumber());
        insertCarPreparedStatement.setInt(2, car.getYearMade());
        insertCarPreparedStatement.setInt(3, car.getQuantity());
        insertCarPreparedStatement.setString(4, car.getColor1());
        insertCarPreparedStatement.setString(5, car.getColor2());
        insertCarPreparedStatement.setString(6, car.getColor3());
        insertCarPreparedStatement.setString(7, car.getMaker());
        insertCarPreparedStatement.setString(8, car.getModel());
        insertCarPreparedStatement.setInt(9, car.getPrice());
        insertCarPreparedStatement.setBytes(10, car.getImageBytes());
        insertCarPreparedStatement.executeUpdate();
    }

    public static String addCar(Car car) {
        String response;
        carListReadWriteLock.writeLock().lock();
        try {
            int checkCarExists = checkCarExists(car);
            // car with same regNo but different fields
            if (checkCarExists == Protocol.UNEQUAL_CAR_EXISTS) {
                response = Protocol.ADD_CAR_EXISTS;
            } else {
                if (checkCarExists == Protocol.EQUAL_CAR_EXISTS) {
                    int index;
                    for (index = 0; index < carList.size(); index++) {
                        if (carList.get(index).getRegistrationNumber().equals(car.getRegistrationNumber())) {
                            break;
                        }
                    }
                    Car target = carList.get(index);
                    carList.remove(index);
                    target.setQuantity(target.getQuantity() + car.getQuantity());
                    System.out.println(target);
                    carList.add(target);
                } else {
                    carList.add(car);
                }
                response = Protocol.ADD_SUCCESS;
            }
        } finally {
            carListReadWriteLock.writeLock().unlock();
        }
        return response;
    }

    private static int checkCarExists(Car car) {
        int check = 0; // new registration number

        Car sameRegCar = null;
        for (Car car1 : carList) {
            if (car1.getRegistrationNumber().equals(car.getRegistrationNumber())) {
                sameRegCar = car1;
                break;
            }
        }
        if (sameRegCar != null) {
            // there can be only one car with one registration number
            if (car.equals(sameRegCar)) {
                check = Protocol.EQUAL_CAR_EXISTS;
            } else {
                check = Protocol.UNEQUAL_CAR_EXISTS;
            }
        }
        return check;
    }

    public static String deleteCar(String registrationNum) {
        carListReadWriteLock.writeLock().lock();
        boolean deleteSuccess;
        try {
            deleteSuccess = carList.removeIf(car ->
                    car.getRegistrationNumber().equals(registrationNum));
        } finally {
            carListReadWriteLock.writeLock().unlock();
        }
        String response;
        if (deleteSuccess) {
            response = Protocol.DELETE_SUCCESS;
        } else {
            response = Protocol.DELETE_FAIL;
        }
        return response;
    }

    public static String buyCar(String regNo) {
        String response = Protocol.BUY_FAILURE;
        carListReadWriteLock.writeLock().lock();
        try {
            for (Car car1 : carList) {
                if (car1.getRegistrationNumber().equals(regNo)) {
                    car1.setQuantity(car1.getQuantity() - 1);
                    if (car1.getQuantity() == 0) {
                        deleteCar(regNo);
                    }
                    response = Protocol.BUY_SUCCESS;
                    break;
                }
            }
        } finally {
            carListReadWriteLock.writeLock().unlock();
        }
        return response;
    }

    public static List<Car> getAllCars() {
        carListReadWriteLock.readLock().lock();
        List<Car> cars;
        try {
            cars = new ArrayList<>(carList.size());
            cars.addAll(carList);
        } finally {
            carListReadWriteLock.readLock().unlock();
        }
        cars.forEach(System.out::println);
        return cars;
    }

    public static boolean verifyUsernameAndPassword(String username, String password) {
        boolean matched = false;
        accountListReadWriteLock.readLock().lock();
        try {
            for (String[] account : accountList) {
                if (account[0].equals(username)) {
                    if (account[2].equals(Protocol.TYPE_MANUFACTURER)) {
                        matched = account[1].equals(password);
                    } else {
                        matched = true;
                    }
                    break;
                }
            }
        } finally {
            accountListReadWriteLock.readLock().unlock();
        }
        return matched;
    }

    public static String editCar(Car editedCar) {
        carListReadWriteLock.writeLock().lock();
        String response;
        try {
            Optional<Car> carOptional = carList.stream()
                    .filter(car -> car.getRegistrationNumber()
                            .equals(editedCar.getRegistrationNumber())).findFirst();
            if (carOptional.isPresent()) {
                Car car = carOptional.get();
                car.setEditableFieldsFrom(editedCar);
                response = Protocol.EDIT_SUCCESS;
            } else {
                response = Protocol.EDIT_FAIL;
            }
        } finally {
            carListReadWriteLock.writeLock().unlock();
        }
        return response;
    }

    public static String addUser(String name, String password, String type) {
        String response;
        Optional<String[]> userNameCheckOptional = accountList.stream()
                .filter(strings -> strings[0].equals(name)).findAny();
        if (userNameCheckOptional.isEmpty()) {
            accountList.add(new String[]{name, password, type});
            response = Protocol.ADD_USER_SUCCESS;
        } else {
            response = Protocol.ADD_USER_FAILURE;
        }
        return response;
    }

    public static List<String[]> getAccountList() {
        return accountList;
    }

    public static void deleteAccount(String username) {
        accountList.removeIf(strings -> strings[0].equals(username));
    }
}
