package com.github.damiox.ecommerce.api.controller.performance.capacity;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import org.junit.Rule;

import static com.github.damiox.ecommerce.api.controller.performance.capacity.CapacityTestCoordinator.RESULT_PATH;
import static com.github.damiox.ecommerce.api.controller.performance.capacity.CapacityTestCoordinator.TARGET_PID;

public class CapacityIntegrationTestBase extends IntegrationTestBase {
    public static final int ITERATIONS = 10;

    @Rule
    public CapacityTrackerRule capacityTrackerRule = new CapacityTrackerRule(ITERATIONS, TARGET_PID, RESULT_PATH);
}
