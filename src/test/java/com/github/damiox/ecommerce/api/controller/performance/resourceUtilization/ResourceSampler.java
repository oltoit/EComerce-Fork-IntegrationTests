package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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
        try {
            psrecordProcess = new ProcessBuilder(
                    "nice", "-n", "20",
                    psrecordPath, String.valueOf(targetPid),
                    "--interval", "" + rate,
                    "--log", outputPath + "/" + testName + "-" + iteration + ".txt"
            ).start();
            // FIXME: for endpoints with low latency have to wait until the measurements have started
        } catch (IOException e) {
            throw new RuntimeException("psrecord was not found", e);
        }
    }

    public static void stop() {
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