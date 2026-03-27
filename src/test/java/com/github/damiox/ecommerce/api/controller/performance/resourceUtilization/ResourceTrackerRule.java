package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ResourceTrackerRule implements TestRule {
    private final int repetitions;

    public ResourceTrackerRule(int repetitions, long pid, String dirPath) {
        this.repetitions = repetitions;
        ResourceSampler.setPid(pid);
        ResourceSampler.setDirPath(dirPath);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                for (int i = 0; i < repetitions; i++) {
                    ResourceSampler.setIteration(i);
                    base.evaluate();
                }
            }
        };
    }
}
