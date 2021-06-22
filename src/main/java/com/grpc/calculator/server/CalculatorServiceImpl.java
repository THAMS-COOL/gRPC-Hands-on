package com.grpc.calculator.server;

import com.proto.calculator.*;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
    @Override
    public void calculator(CalculatorRequest request, StreamObserver<CalculatorResponse> responseObserver) {
     //   super.calculator(request, responseObserver);
        Calculator calculator = request.getCalculator();
        int num_1 = calculator.getNum1();
        int num_2 = calculator.getNum2();

        int sum = num_1 + num_2;
        CalculatorResponse response = CalculatorResponse.newBuilder()
                                        .setSum(sum)
                                        .build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();

    }

    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {
        Integer number = request.getNumber();
        Integer divisor = 2;

        while (number > 1) {
            if (number % divisor == 0) {
                number = number / divisor;
                responseObserver.onNext(PrimeNumberDecompositionResponse.newBuilder()
                        .setPrimeFactor(divisor)
                        .build());
            } else {
                divisor = divisor + 1;
                System.out.println(divisor);
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {
        return new StreamObserver<ComputeAverageRequest>() {
            double average;
            int sum =0 ;
            int count = 0;
            @Override
            public void onNext(ComputeAverageRequest value) {
                // client sends a message
//                number += value.getNumber();
                sum += value.getNumber();
                System.out.println(sum);
                count += 1;

            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                average = sum /count;
                System.out.println("Average: " + average);
                    responseObserver.onNext(
                            ComputeAverageResponse.newBuilder()
                                    .setAverage(average)
                                    .build()
                    );
                    responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        return new StreamObserver<FindMaximumRequest>() {
            int currentMaximum = 0;
            @Override
            public void onNext(FindMaximumRequest value) {
                int currentNumber = value.getNumber();

                if (currentNumber > currentMaximum) {
                    currentMaximum = currentNumber;
                    responseObserver.onNext(
                            FindMaximumResponse.newBuilder()
                                    .setMaximum(currentMaximum)
                                    .build()
                    );
                } else {
                    // nothing
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                // send the current last maximum
                responseObserver.onNext(
                        FindMaximumResponse.newBuilder()
                                .setMaximum(currentMaximum)
                                .build());
                // the server is done sending data
                responseObserver.onCompleted();
            }
        };
    }
}
