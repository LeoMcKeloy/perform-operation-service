package lev.pyryanov.perform.operation.service.impl;

import lev.pyryanov.perform.operation.client.Client;
import lev.pyryanov.perform.operation.model.ApplicationStatusResponse;
import lev.pyryanov.perform.operation.model.Response;
import lev.pyryanov.perform.operation.service.Handler;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class HandlerImpl implements Handler {

    private final Client client;
    private static final int DEFAULT_TIMEOUT = 15;

    public HandlerImpl(Client client) {
        this.client = client;
    }

    @Override
    public ApplicationStatusResponse performOperation(String id) {
        Supplier<Response> codeToRun1 = () -> client.getApplicationStatus1(id);
        Supplier<Response> codeToRun2 = () -> client.getApplicationStatus2(id);

        int[] retries = new int[]{1};
        CompletableFuture<Response> future1 = retryTask(codeToRun1, retries);
        CompletableFuture<Response> future2 = retryTask(codeToRun2, retries);

        try {
            Object result = CompletableFuture.anyOf(future1, future2).get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            if (result instanceof Response.Success success) {
                return new ApplicationStatusResponse.Success(success.applicationId(), success.applicationStatus());
            } else {
                return new ApplicationStatusResponse.Failure(Duration.ZERO, retries[0]);
            }
        } catch (Exception e) {
            return new ApplicationStatusResponse.Failure(Duration.ZERO, retries[0]);
        }
    }

    private static <T> Supplier<T> retryFunction(Supplier<T> supplier, int[] retries) {
        return () -> {
            while (true) {
                T cur = supplier.get();
                if (cur instanceof Response.RetryAfter) {
                    retries[0]++;
                } else {
                    return cur;
                }
            }
        };
    }

    private static <T> CompletableFuture<T> retryTask(Supplier<T> supplier, int[] retries) {
        Supplier<T> retryableSupplier = retryFunction(supplier, retries);
        return CompletableFuture.supplyAsync(retryableSupplier);
    }
}
