package com.grpc.calculator.server;

import com.grpc.greeting.server.GreetServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class CalculatorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello gRPC calculator service");

        Server server = ServerBuilder.forPort(50052)
                .addService(new CalculatorServiceImpl())
                .build();

        server.start();


        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> {
                    System.out.println("Received ShoutDown Request");
                    server.shutdown();
                    System.out.println("Successfully stopped the server");
                }
        ));

        // without this method --> the server starts and finishes
        // in gRPC this server thread needs to be blocking for the main thread for the awaitTermination
        // so we do server.awaitTermination()
        server.awaitTermination(); // blocks the main thread
    }
}
