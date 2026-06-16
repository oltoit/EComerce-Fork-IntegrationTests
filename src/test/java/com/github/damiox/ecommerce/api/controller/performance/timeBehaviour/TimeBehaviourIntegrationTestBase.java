package com.github.damiox.ecommerce.api.controller.performance.timeBehaviour;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import org.junit.Rule;

public class TimeBehaviourIntegrationTestBase extends IntegrationTestBase {
    public static final int ITERATIONS = 100;

    @Rule
    public TimeTrackerRule timeTrackerRule = new TimeTrackerRule(ITERATIONS, TimeTestCoordinator.CSV_PATH);
}
