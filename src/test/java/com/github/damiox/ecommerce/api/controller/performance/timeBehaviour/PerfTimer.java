package com.github.damiox.ecommerce.api.controller.performance.timeBehaviour;

/// measures time in tests
/// measures time in nanoseconds
public class PerfTimer {

    private static final ThreadLocal<Long> startTime = new ThreadLocal<>();
    private static final ThreadLocal<Long> duration = new ThreadLocal<>();

    public static void start() {
        startTime.set(System.nanoTime());
    }

    public static void stop() {
        duration.set(System.nanoTime() - startTime.get());
    }

    public static long getDuration() {
        return duration.get();
    }

    public static void reset() {
        startTime.remove();
        duration.remove();
    }
}