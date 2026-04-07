package com.github.damiox.ecommerce.api.controller.performance.capacity.tests;

import com.github.damiox.ecommerce.api.controller.objects.ProductDto;
import com.github.damiox.ecommerce.api.controller.objects.Role;
import com.github.damiox.ecommerce.api.controller.objects.User;
import com.github.damiox.ecommerce.api.controller.performance.capacity.CapacityIntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.performance.capacity.CapacitySampler;
import com.github.damiox.ecommerce.api.controller.performance.capacity.CapacityTestCoordinator;
import com.github.damiox.ecommerce.api.controller.utils.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GatlingCallerTest extends CapacityIntegrationTestBase {

    public static final String LOADTEST_USERNAME = "loadtest-user-";
    public static final String LOADTEST_PASSWORD = "loadtest-password-";
    public static final String LOADTEST_FOLDER = CapacityTestCoordinator.RESULT_PATH;

    @Autowired
    private UserUtils userUtils;
    @Autowired
    private ProductUtils productUtils;
    @Autowired
    private CategoryUtils categoryUtils;
    @Autowired
    private ProductCategoryUtils productCategoryUtils;

    @BeforeClass
    public static void starteWholeRun(){
        CapacitySampler.startWholeRun("whole-run", 2_400);
    }

    @AfterClass
    public static void stopWholeRun() {
        CapacitySampler.stop();
    }

    @Test
    public void order1_gatlingTest_10Users() {
        int users = 10;
        int ramp = 1;
        String name = Thread.currentThread().getStackTrace()[1].getMethodName();
        int hertz = 2_400;

        executeTest(users, ramp, name, hertz);
    }

    @Test
    public void order2_gatlingTest_50Users() {
        int users = 50;
        int ramp = 5;
        String name = Thread.currentThread().getStackTrace()[1].getMethodName();
        int hertz = 2_400;

        executeTest(users, ramp, name, hertz);
    }

    @Test
    public void order3_gatlingTest_100Users() {
        int users = 100;
        int ramp = 10;
        String name = Thread.currentThread().getStackTrace()[1].getMethodName();
        int hertz = 2_400;

        executeTest(users, ramp, name, hertz);
    }

    @Test
    public void order4_gatlingTest_250Users() {
        int users = 250;
        int ramp = 25;
        String name = Thread.currentThread().getStackTrace()[1].getMethodName();
        int hertz = 2_400;

        executeTest(users, ramp, name, hertz);
    }

    @Test
    public void order5_gatlingTest_500Users() {
        int users = 500;
        int ramp = 50;
        String name = Thread.currentThread().getStackTrace()[1].getMethodName();
        int hertz = 2_400;

        executeTest(users, ramp, name, hertz);
    }

    @Test
    public void order6_gatlingTest_1000Users() {
        int users = 1_000;
        int ramp = 100;
        String name = Thread.currentThread().getStackTrace()[1].getMethodName();
        int hertz = 2_400;

        executeTest(users, ramp, name, hertz);
    }

    private void executeTest(int users, int ramp, String name, int hertz) {
        prepareContext(users, ramp);
        new File(getGatlingOutputPath(name)).mkdirs();

        // execute test
        try {
            runGatling(name);
        } catch(IOException | InterruptedException e) {throw new RuntimeException(e);}
    }

    // Helper functions

    private void prepareContext(int users, int ramp) {
        setSystemPropertiesGatling(users, ramp);
        createTestUsers(users);

        for (int i = 0; i < 1_000; i++) {
            long productId = productUtils.createProduct(new ProductDto("loading-test-product" + i, "EUR", 10.00), user1.id);
            long categoryId = categoryUtils.createCategory("loading-test-category" + i);
            if (i % 2 == 0) {
                productCategoryUtils.addProductToCategory(productId, categoryId);
            }
            if (i % 4 == 0) {
                categoryUtils.createSubcategory("loading-test-subcategory" + i, categoryId);
            }
        }
    }

    // create all test-users as admins so that every endpoint can be tested
    private void createTestUsers(int num) {
        for (int i = 0; i < num; i++) {
            String name = LOADTEST_USERNAME + i;
            String password = LOADTEST_PASSWORD + i;
            userUtils.createUser(new User(name, password, Role.ADMIN, i));
        }
    }

    private void setSystemPropertiesGatling(int users, int rampSecs) {
        System.setProperty("baseUrl", "http://localhost:8080");
        System.setProperty("users", "" + users);
        System.setProperty("rampSecs", "" + rampSecs);
    }

    private void runGatling(String testName) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(
                "mvn", "gatling:test",
                "-Dgatling.simulationClass=" + ECommerceSimulation.class.getName(),
                "-Dgatling.resultsFolder=" + getGatlingOutputPath(testName),
                "-DbaseUrl=" + System.getProperty("baseUrl"),
                "-Dusers=" + System.getProperty("users"),
                "-DrampSecs=" + System.getProperty("rampSecs")
        );
        pb.inheritIO();
        pb.directory(new File("."));

        Process process = pb.start();
        int exitCode = process.waitFor();

        assertThat(exitCode).isEqualTo(0);
    }

    private static String getGatlingOutputPath(String testName) {
        return  LOADTEST_FOLDER + "/" + testName;
    }
}
