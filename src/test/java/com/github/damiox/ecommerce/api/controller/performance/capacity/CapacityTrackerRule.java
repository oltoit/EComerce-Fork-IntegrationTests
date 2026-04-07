package com.github.damiox.ecommerce.api.controller.performance.capacity;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class CapacityTrackerRule implements TestRule {
    private int iterations;

    public CapacityTrackerRule(int iterations, long pid, String dir) {
        this.iterations = iterations;
        CapacitySampler.setPid(pid);
        CapacitySampler.setDirPath(dir);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
            for (int i = 0; i < iterations; i++) {
                CapacitySampler.setIteration(i);
                base.evaluate();
            }
            }
        };
    }
}
