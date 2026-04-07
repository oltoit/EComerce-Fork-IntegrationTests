package com.github.damiox.ecommerce.api.controller.performance.capacity;

import com.github.damiox.ecommerce.api.controller.performance.capacity.tests.GatlingCallerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    GatlingCallerTest.class
})
public class CapacityTestCoordinator {
    public final static long TARGET_PID = findTargetPid();
    public final static String RESULT_PATH = createResultDir();

    static {
        CapacitySampler.setPid(CapacityTestCoordinator.TARGET_PID);
        CapacitySampler.setDirPath(RESULT_PATH);
    }

    public static String createResultDir() {
        String baseDir = System.getProperty("user.dir") + "/target/performance-test/capacity";
        new File(baseDir).mkdirs();

        int dirCount = new File(baseDir).list().length;
        String newTestDir =  baseDir + "/capacity-results-" + dirCount + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        new File(newTestDir).mkdirs();

        return newTestDir;
    }

    private static long findTargetPid() {
        try {
            int port = 8080;
            ProcessBuilder pb = new ProcessBuilder("zsh", "-lc", "lsof -ti tcp:" + port);
            Process p = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();
                if (line == null || line.isEmpty()) {
                    throw new RuntimeException("No process on port " + port);
                }
                return Long.parseLong(line.trim());
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("PID could not be found", e);
        }
    }
}
