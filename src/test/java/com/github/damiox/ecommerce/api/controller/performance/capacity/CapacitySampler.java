package com.github.damiox.ecommerce.api.controller.performance.capacity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CapacitySampler {

    private static final String MEASURING_PATH = System.getProperty("user.dir") + "/data-helpers/gatling-helpers/perf-measure.py";

    private static BufferedReader measurementReader;
    private static Process process;
    private static long targetPid = - 1;
    private static String outputPath;
    private static int iteration = 0;

    public static void setPid(long pid) {
        if (targetPid == -1) {
            targetPid = pid;
        }
    }

    public static void setDirPath(String dirPath) {
        if (outputPath == null) {
            outputPath = dirPath;
        }
    }

    public static void setIteration(int i) {
        iteration = i;
    }

    public static void startWholeRun(String runName) {
        File logFile = new File(outputPath + "/" + runName + iteration + "-T" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH-mm-ss")) + ".csv");
        logFile.getParentFile().mkdirs();
        try {
            process = new ProcessBuilder(
                    "python3", MEASURING_PATH, String.valueOf(targetPid),
                    "--output", logFile.getAbsolutePath()
            )
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start();

            measurementReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            waitForReadySignal();
        } catch (IOException e) {
            throw new RuntimeException("measuring utility was not found", e);
        }
    }

    private static void waitForReadySignal() {
        try {
            String line;
            while ((line = measurementReader.readLine()) != null) {
                if ("READY".equals(line.trim())) {
                    return;
                }
            }
            throw new RuntimeException("Measurement process terminated before READY signal");
        } catch (IOException e) {
            throw new RuntimeException("Failed while waiting for READY signal", e);
        }
    }

    public static void stop() {
        if (process == null) return;
        try {
            if (process.isAlive()) {
                process.destroy();
                if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    process.waitFor();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while stopping measurement process", e);
        } finally {
            try {
                if (measurementReader != null) {
                    measurementReader.close();
                }
            } catch (IOException ignored) {}
            measurementReader = null;
            process = null;
        }
    }
}
