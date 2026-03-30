package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization;

import com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.tests.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    CategoryResourceTest.class,
    ProductResourceTest.class,
    CategorySubcategoriesResourceTest.class,
    CategoryProductsResourceTest.class,
    AuthenticationResourceTest.class
})
public class ResourceTestCoordinator {
    public final static long TARGET_PID = findTargetPid();
    public final static String TEST_DIR = createDir();

    private static String createDir() {
        String baseDir = System.getProperty("user.dir") + "/target/performance-test/resource-utilization";
        new File(baseDir).mkdirs();

        int dirCount = new File(baseDir).list().length;
        String newTestDir =  baseDir + "/resource-results-" + dirCount + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
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