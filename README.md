# CarWarehouse

A UI based implementation of multithreaded Client-Server networking using socket programming in Java.

## About The Project
This project has a client application and a server application. 

### Server
![server](https://github.com/salkhon/CarWarehouse/blob/master/src/com/carwarehouse/icons/server.png)

The server holds information about clients and various cars along with their images. It accepts connection from 
multiple clients. 


### Client
![client](https://github.com/salkhon/CarWarehouse/blob/master/src/com/carwarehouse/icons/client.png)

The client applications can view those data fetched from the server using sockets. Clients can also modify 
the server data in a **thread-safe** manner. They can search cars by make, model, and registration number. 

## Built With
* [JDK 11](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)
* [JavaFX 11](https://openjfx.io/)
* [sqlite-jdbc-3.30.1](https://www.sqlitetutorial.net/sqlite-java/sqlite-jdbc-driver/)

## Getting Started
To run the application you have to have the dependencies installed and set up in your IDE. 

### Installation
1. Clone the repo
```
git clone git@github.com:salkhon/CarWarehouse.git
```
2. Create a sqlite database file anywhere in your machine.
3. Set up the connection string of that .db file with the scheduler. Navigate to
   `CarWarehouse\src\com\carwarehouse\server\model\ServerDatabase.java` and set the `CONNECTION_STRING` variable with this format: `jdbc:sqlite:<PATH TO YOUR .db FILE>`.
4. Now run server `CarWarehouse\src\com\carwarehouse\server\ServerMain.java` and client `CarWarehouse\src\com\carwarehouse\client\ClientMain.java`.