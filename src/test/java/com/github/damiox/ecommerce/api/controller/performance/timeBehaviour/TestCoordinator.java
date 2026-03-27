package com.github.damiox.ecommerce.api.controller.performance.timeBehaviour;

import com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.tests.CategoryPerfTimeTest;
import com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.tests.CategoryProductsPerfTimeTest;
import com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.tests.CategorySubcategoriesPerfTimeTest;
import com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.tests.ProductPerfTimeTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    CategoryPerfTimeTest.class,
    ProductPerfTimeTest.class,
    CategorySubcategoriesPerfTimeTest.class,
    CategoryProductsPerfTimeTest.class,
})
public class TestCoordinator {
    public static final String CSV_PATH = createCsvPath();

    private static String createCsvPath() {
        String dir = "target/performance-test/time-behaviour";
        new File(dir).mkdirs();

        int fileCount = new File(dir).list().length;
        String csvPath = dir + "/perf-results-" + fileCount + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".csv";

        new File(csvPath);
        return csvPath;
    }
}