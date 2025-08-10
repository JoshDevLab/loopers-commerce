package com.loopers.support.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class ConcurrentTestUtils {
    public static Result runConcurrent(int threadCount, Runnable task) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        AtomicLong success = new AtomicLong(0);
        AtomicLong failed = new AtomicLong(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    task.run();
                    success.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                    failed.incrementAndGet();
                }
            }, executorService);
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();

        return new Result(success.get(), failed.get());
    }

    public record Result(long successCount, long failedCount) {}
}
