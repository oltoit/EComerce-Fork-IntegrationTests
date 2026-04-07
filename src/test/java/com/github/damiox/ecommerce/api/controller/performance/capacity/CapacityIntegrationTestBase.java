package com.github.damiox.ecommerce.api.controller.performance.capacity;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import org.junit.Rule;

import static com.github.damiox.ecommerce.api.controller.performance.capacity.CapacityTestCoordinator.RESULT_PATH;
import static com.github.damiox.ecommerce.api.controller.performance.capacity.CapacityTestCoordinator.TARGET_PID;

public class CapacityIntegrationTestBase extends IntegrationTestBase {
    @Rule
    public CapacityTrackerRule capacityTrackerRule = new CapacityTrackerRule(1, TARGET_PID, RESULT_PATH);
}
