package com.grpc.calculator.client;

import com.grpc.greeting.client.GreetingClient;
import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {
    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC calculator client");
        CalculatorClient main = new CalculatorClient();
        main.run();
    }
    private void run(){
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

//        doUnaryCall(channel);

//        doServerStreamingCall(channel);

//          doClientStreamingCall(channel);

          doBiDiStreamingCall(channel);

        System.out.println("Shutting down channel");
        channel.shutdown();
    }


    private void doUnaryCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient =
                CalculatorServiceGrpc.newBlockingStub(channel);
                Calculator calculator = Calculator.newBuilder()
                                .setNum1(10)
                                .setNum2(20)
                                .build();

        CalculatorRequest calculatorRequest = CalculatorRequest.newBuilder()
                                                .setCalculator(calculator)
                                                .build();

        System.out.println("Calculator Request: " + calculatorRequest.getCalculator());
        CalculatorResponse calculatorResponse = calculatorClient.calculator(calculatorRequest);

        System.out.println("Calculator Response: " + calculatorResponse);

        System.out.println("Sum: " + calculatorResponse.getSum());
    }

    private void doServerStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceBlockingStub calculatorClient =
                CalculatorServiceGrpc.newBlockingStub(channel);
        Integer number = 567890;
        calculatorClient.primeNumberDecomposition(PrimeNumberDecompositionRequest.newBuilder()
                .setNumber(number).build())
                .forEachRemaining(PrimeNumberDecompositionResponse ->{
                    System.out.println(PrimeNumberDecompositionResponse.getPrimeFactor());
                });

    }

    private void doClientStreamingCall(ManagedChannel channel) {

        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<ComputeAverageRequest> requestObserver = asyncClient.computeAverage(new StreamObserver<ComputeAverageResponse>() {
            @Override
            public void onNext(ComputeAverageResponse value) {
                System.out.println("Received a response from the server");
                System.out.println(value.getAverage());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending us something");
                latch.countDown();
            }
        });

        // streaming message #1
        System.out.println("sending message 1");
        requestObserver.onNext(
                ComputeAverageRequest.newBuilder()
                .setNumber(10)
                .build()
        );
        // streaming message #2
        System.out.println("sending message 2");
        requestObserver.onNext(
                ComputeAverageRequest.newBuilder()
                        .setNumber(20)
                        .build()
        );
        // streaming message #3
        System.out.println("sending message 3");
        requestObserver.onNext(
                ComputeAverageRequest.newBuilder()
                        .setNumber(30)
                        .build()
        );

        // streaming message #4
        System.out.println("sending message 4");
        requestObserver.onNext(
                ComputeAverageRequest.newBuilder()
                        .setNumber(40)
                        .build()
        );

        // we tell the server that client is done sending data
        requestObserver.onCompleted();

        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        CalculatorServiceGrpc.CalculatorServiceStub asyncClient = CalculatorServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);


        StreamObserver<FindMaximumRequest> requestObserver = asyncClient.findMaximum(new StreamObserver<FindMaximumResponse>() {
            @Override
            public void onNext(FindMaximumResponse value) {
                System.out.println("Got new maximum from Server: " + value.getMaximum());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending messages");
            }
        });


        Arrays.asList(3, 5, 17, 9, 8, 30, 12).forEach(
                (number) -> {
                    System.out.println("Sending number: " + number);
                    requestObserver.onNext(FindMaximumRequest.newBuilder()
                            .setNumber(number)
                            .build());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        requestObserver.onCompleted();

        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
