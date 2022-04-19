package com.carwarehouse.server;

import com.carwarehouse.server.controller.ServerStatsController;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerDispatcher implements Runnable {
    private final int port = 5000;
    private ServerStatsController serverStatsController;

    private final ExecutorService executorService;
    private final List<ServerUnit> serverUnitList;

    public ServerDispatcher(ServerStatsController serverStatsController) {
        this.serverStatsController = serverStatsController;
        this.executorService = Executors.newCachedThreadPool();
        this.serverUnitList = new LinkedList<>();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(this.port)) {
            while (!this.executorService.isShutdown()) {
                Socket socket = serverSocket.accept();
                if (!this.executorService.isShutdown()) {
                    try {
                        ServerUnit serverUnit = new ServerUnit(socket);
                        this.executorService.submit(serverUnit);
                        this.serverUnitList.add(serverUnit);
                    } catch (IOException ioException1) {
                        System.out.println("COULD NOT OPEN SERVER UNIT: " + ioException1.getMessage());
                    }
                }
            }
        } catch (IOException ioException) {
            System.out.println("CANNOT RUN SERVER DISPATCHER: " + ioException.getMessage());
        }
    }

    public void stop() {
        this.serverUnitList.forEach(serverUnit -> {
            try {
                serverUnit.close();
            } catch (IOException ioException) {
                System.out.println("COULD NOT CLOSE SERVER UNITS: " + ioException.getMessage());
            }
        });

        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                for (Runnable runnable : this.executorService.shutdownNow()) {
                    ((ServerUnit) runnable).interrupt();
                    try {
                        ((ServerUnit) runnable).close();
                    } catch (IOException ioException) {
                        System.out.println("COULD NOT CLOSE SERVER UNITS: " + ioException.getMessage());
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
