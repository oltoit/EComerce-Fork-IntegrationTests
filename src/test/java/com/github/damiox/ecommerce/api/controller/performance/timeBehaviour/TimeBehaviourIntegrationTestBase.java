package com.github.damiox.ecommerce.api.controller.performance.timeBehaviour;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import org.junit.Rule;

public class TimeBehaviourIntegrationTestBase extends IntegrationTestBase {
    @Rule
    public TimeTrackerRule timeTrackerRule = new TimeTrackerRule(10, TimeTestCoordinator.CSV_PATH);
}
