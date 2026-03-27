package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/// Class running in separate thread that measures resource utilization.
/// measures cpu-utilization in % and ram-usage in MB.
/// This sampler is written for macos, it was not tested for any other system.
/// It uses the application ```psrecord``` which can be installed through pip.
public class ResourceSampler {

    private static Process psrecordProcess;
    private static long targetPid = - 1;
    private static String outputPath;

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

//    public static void start(String testName) {
//        String psrecordPath = findPsRecord();
//        try {
//            psrecordProcess = new ProcessBuilder(
//                    psrecordPath, String.valueOf(targetPid),
//                    "--interval", "0.033",
//                    "--log", outputPath + "/" + testName + ".txt"
//            ).start();
//        } catch (IOException e) {
//            throw new RuntimeException("psrecord was not found", e);
//        }
//    }

    public static void start(String testName) {
        String psrecordPath = findPsRecord();
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    psrecordPath, String.valueOf(targetPid),
                    "--interval", "0.033",
                    "--log", outputPath + "/" + testName + ".txt"
            );
            pb.redirectErrorStream(true); // stderr → stdout
            psrecordProcess = pb.start();

            // Output von psrecord in separatem Thread lesen und loggen
            Process finalProcess = psrecordProcess;
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(finalProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[psrecord] " + line);
                    }
                } catch (IOException e) {
                    System.err.println("[psrecord] Fehler beim Lesen des Outputs: " + e.getMessage());
                }
            }).start();

        } catch (IOException e) {
            throw new RuntimeException("psrecord konnte nicht gestartet werden", e);
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