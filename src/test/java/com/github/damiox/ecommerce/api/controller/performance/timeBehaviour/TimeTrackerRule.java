package com.github.damiox.ecommerce.api.controller.performance.timeBehaviour;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TimeTrackerRule implements TestRule {
    private final int repetitions;
    private final String csvPath;

    public TimeTrackerRule(int repetitions, String csvPath) {
        this.repetitions = repetitions;
        this.csvPath = csvPath;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                String testName = description.getMethodName();

                for (int i = 0; i < repetitions; i++) {
                    base.evaluate();
                    writeToCsv(testName, PerfTimer.getDuration());
                    // reset timer -> if csv contains negative value in duration something is wrong
                    PerfTimer.reset();
                }
            }
        };
    }

    private void writeToCsv(String testName, long durationMs) throws IOException {
        File file = new File(csvPath);
        List<String> lines = new ArrayList<>();
        if (file.exists()) {
            lines = Files.readAllLines(file.toPath());
        }

        boolean found = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith(testName + ",")) {
                lines.set(i, line + "," + durationMs);
                found = true;
                break;
            }
        }
        if (!found) {
            lines.add(testName + "," + durationMs);
        }
        Files.write(file.toPath(), lines);
    }
}
