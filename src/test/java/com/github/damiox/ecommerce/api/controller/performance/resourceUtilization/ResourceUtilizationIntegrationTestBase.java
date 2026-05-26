package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import org.junit.Rule;

import static com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceTestCoordinator.TARGET_PID;
import static com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceTestCoordinator.TEST_DIR;

public class ResourceUtilizationIntegrationTestBase extends IntegrationTestBase {
    public static final int REPETITIONS = 100;

    @Rule
    public ResourceTrackerRule resourceTrackerRule = new ResourceTrackerRule(REPETITIONS, TARGET_PID, TEST_DIR);
}
