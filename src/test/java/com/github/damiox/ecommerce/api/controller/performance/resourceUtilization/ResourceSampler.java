package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/// Class running in separate thread that measures resource utilization.
/// measures cpu-utilization in % and ram-usage in MB.
/// This sampler is written for macOS, it was not tested for any other system.
/// It uses the application ```psrecord``` which can be installed through pip.
public class ResourceSampler {

    private static final String psrecordPath = findPsRecord();

    private static Process psrecordProcess;
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

    public static void start(String testName, int hertz) {
        double rate = 1.0 / hertz;
        File logFile = new File(outputPath + "/" + testName + "-" + iteration + ".txt");
        try {
            psrecordProcess = new ProcessBuilder(
                    psrecordPath, String.valueOf(targetPid),
                    "--interval", "" + rate,
                    "--log", logFile.getAbsolutePath()
            ).start();
            waitForFileCreation(logFile);
        } catch (IOException e) {
            throw new RuntimeException("psrecord was not found", e);
        }
    }

    private static void waitForFileCreation(File file) {
        while (true) {
            // if log file has been created and something has been written psrecord is ready -> request can be sent now
            if (file.exists() && file.length() > 0) {
                return;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for psrecord to initialize", e);
            }
        }
    }

    public static void stop() {
        // Wait for 200 millis so, psrecords shutdown doesn't collide with application measurements
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {throw new RuntimeException(e);}
        if (psrecordProcess != null && psrecordProcess.isAlive()) {
            psrecordProcess.destroy();
        }
    }

    private static String findPsRecord() {
        String[] candidates = {
                "/usr/local/bin/psrecord",
                "/opt/homebrew/bin/psrecord",
                System.getProperty("user.home") + "/Library/Python/3.9/bin/psrecord",
                System.getProperty("user.home") + "/.local/bin/psrecord"
        };

        for (String candidate : candidates) {
            if (new File(candidate).exists()) {
                return candidate;
            }
        }
        throw new RuntimeException("psrecord was not found - is it installed?");
    }
}