package com.carwarehouse;

public interface Protocol {
    String DELIM = " ";

    String TYPE_VIEWER = "Viewer";
    String TYPE_MANUFACTURER = "Manufacturer";

    String LOGIN = "LOGIN: ";
    String LOGIN_SUCCESSFUL = LOGIN + "successful";
    String LOGIN_FAILURE = LOGIN + "failure";
    // LOGIN: username password type [client]
    // LOGIN: successful OR LOGIN: failure [server]

    String LOGOUT = "LOGOUT";

    String GET = "GET: ";
    String GET_ALL_CARS = GET + "all cars";

    String ADD = "ADD: ";
    // followed by a car object
    String ADD_SUCCESS = ADD + "success";
    String ADD_CAR_EXISTS = ADD + "car exists";
    // server add car
    int UNEQUAL_CAR_EXISTS = 1;
    int EQUAL_CAR_EXISTS = 2;

    String DELETE = "DELETE: ";
    // DELETE: Registration_Number
    String DELETE_SUCCESS = DELETE + "success";
    String DELETE_FAIL = DELETE + "fail";

    String EDIT = "EDIT: ";
    // EDIT:
    // followed by a car object, reg no is uneditable, other edited fields
    String EDIT_FAIL = EDIT + "fail";
    String EDIT_SUCCESS = EDIT + "success";

    String ADD_USER = "ADD_USER: ";
    String ADD_USER_SUCCESS = ADD_USER + "success";
    String ADD_USER_FAILURE = ADD_USER + "failure";

    String BUY_CAR = "BUY_CAR: ";
    // BUY_CAR: registrationNumber
    String BUY_SUCCESS = BUY_CAR + "success";
    String BUY_FAILURE = BUY_CAR + "failure";
}
