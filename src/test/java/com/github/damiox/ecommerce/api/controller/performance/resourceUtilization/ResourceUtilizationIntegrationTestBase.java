package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import org.junit.Rule;

import static com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceTestCoordinator.TARGET_PID;
import static com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceTestCoordinator.TEST_DIR;

public class ResourceUtilizationIntegrationTestBase extends IntegrationTestBase {
    @Rule
    public ResourceTrackerRule resourceTrackerRule = new ResourceTrackerRule(10, TARGET_PID, TEST_DIR);
}
