package com.github.damiox.ecommerce.api.controller.performance.capacity.tests;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.github.damiox.ecommerce.api.controller.performance.capacity.tests.GatlingCallerTest.LOADTEST_PASSWORD;
import static com.github.damiox.ecommerce.api.controller.performance.capacity.tests.GatlingCallerTest.LOADTEST_USERNAME;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class ECommerceSimulation extends Simulation {
    private static final String BASE_URL = System.getProperty("baseUrl","http://localhost:8080");
    private static final int USERS = Integer.parseInt(System.getProperty("users","10"));
    private static final int RAMP_SECS = Integer.parseInt(System.getProperty("rampSecs","5"));

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private static final AtomicInteger counter = new AtomicInteger(0);
    private final Iterator<Map<String, Object>> userFeeder = Stream.generate(() -> {
        int index = counter.getAndIncrement();
        Map<String, Object> data = new HashMap<>();
        data.put("index", index);
        data.put("username", LOADTEST_USERNAME + index);
        data.put("password", LOADTEST_PASSWORD + index);
        return data;
    }).iterator();

    private final ChainBuilder loginChain = exec(http("Login")
            .post("/login")
            .body(StringBody("{\"username\": \"#{username}\", \"password\": \"#{password}\"}"))
            .check(status().is(200))
            .check(jsonPath("$.token").saveAs("jwt")));

    private final ChainBuilder categoryChain = exec(http("GET /categories")
            .get("/categories")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(200))
            .check(jsonPath("$._embedded.categories[0].id").optional().saveAs("categoryId")))
        .pause(Duration.ofMillis(100))
        .exec(http("POST /categories")
            .post("/categories")
            .header("Authorization", "Bearer #{jwt}")
            .body(StringBody("{\"name\": \"load-test-category-#{username}\"}"))
            .check(status().is(201))
            .check(jsonPath("$._links.self.href").find().transform(s -> s.replaceAll(".*/", "")).saveAs("newCategoryId")))
        .pause(Duration.ofMillis(100))
        .exec(http("GET /categories/{id}")
            .get("/categories/#{newCategoryId}")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(200)))
        .pause(Duration.ofMillis(100))
        .exec(http("PUT /categories/{id}")
            .put("/categories/#{newCategoryId}")
            .header("Authorization", "Bearer #{jwt}")
            .body(StringBody("{\"name\": \"updated-category-#{username}\"}"))
            .check(status().is(200)))
        .pause(Duration.ofMillis(100))
        .exec(http("DELETE /categories/{id}")
            .delete("/categories/#{newCategoryId}")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(204)));

    private final ChainBuilder productChain = exec(http("GET /products")
            .get("/products")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(200)))
        .pause(Duration.ofMillis(100))
        .exec(http("POST /products")
            .post("/products")
            .header("Authorization", "Bearer #{jwt}")
            .body(StringBody( "{\"name\": \"load-test-product-#{username}\", \"currency\": \"EUR\", \"price\": 9.99}" ))
            .check(status().is(201))
            .check(jsonPath("$._links.self.href").find().transform(s -> s.replaceAll(".*/", "")).saveAs("newProductId")))
        .pause(Duration.ofMillis(100))
        .exec(http("GET /products/{id}")
            .get("/products/#{newProductId}")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(200)))
        .pause(Duration.ofMillis(100))
        .exec(http("PUT /products/{id}")
            .put("/products/#{newProductId}")
            .header("Authorization", "Bearer #{jwt}")
            .body(StringBody(
                    "{\"name\": \"updated-product-#{username}\", \"currency\": \"USD\", \"price\": 19.99}"
            ))
            .check(status().is(200)))
        .pause(Duration.ofMillis(100))
        .exec(http("DELETE /products/{id}")
            .delete("/products/#{newProductId}")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(204)));

    private final ChainBuilder subCategoryChain = exec(http("POST /categories")
            .post("/categories")
            .header("Authorization", "Bearer #{jwt}")
            .body(StringBody("{\"name\": \"load-test-category-#{username}\"}"))
            .check(status().is(201))
            .check(jsonPath("$._links.self.href").find().transform(s -> s.replaceAll(".*/", "")).saveAs("newCategoryId")))
        .pause(Duration.ofMillis(100))
        .exec(http("POST /categories (sub)")
            .post("/categories")
            .header("Authorization", "Bearer #{jwt}")
            .body(StringBody("{\"name\": \"sub-category-#{username}\"}"))
            .check(status().is(201))
            .check(jsonPath("$._links.self.href").find().transform(s -> s.replaceAll(".*/", "")).saveAs("subCategoryId")))
        .pause(Duration.ofMillis(100))
        .exec(http("POST /categories/{id}/subcategories/{childId}")
            .post("/categories/#{newCategoryId}/subcategories/#{subCategoryId}")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(201)))
        .pause(Duration.ofMillis(100))
        .exec(http("GET /categories/{id}/subcategories")
            .get("/categories/#{newCategoryId}/subcategories")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(200)))
        .pause(Duration.ofMillis(100))
        .exec(http("DELETE /categories/{id} (sub))")
            .delete("/categories/#{subCategoryId}")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(204)))
        .pause(Duration.ofMillis(100))
        .exec(http("DELETE /categories/{id}")
            .delete("/categories/#{newCategoryId}")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(204)));

    private final ChainBuilder productCategoryChain = exec(http("POST /categories")
            .post("/categories")
            .header("Authorization", "Bearer #{jwt}")
            .body(StringBody("{\"name\": \"load-test-category-#{username}\"}"))
            .check(status().is(201))
            .check(jsonPath("$._links.self.href").find().transform(s -> s.replaceAll(".*/", "")).saveAs("newCategoryId")))
        .pause(Duration.ofMillis(100))
        .exec(http("POST /products")
            .post("/products")
            .header("Authorization", "Bearer #{jwt}")
            .body(StringBody(
                    "{\"name\": \"load-test-product-#{username}\", \"currency\": \"EUR\", \"price\": 9.99}"
            ))
            .check(status().is(201))
            .check(jsonPath("$._links.self.href").find().transform(s -> s.replaceAll(".*/", "")).saveAs("newProductId")))
        .pause(Duration.ofMillis(100))
        .exec(http("POST /categories/{id}/products/{productId}")
            .post("/categories/#{newCategoryId}/products/#{newProductId}")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(201)))
        .pause(Duration.ofMillis(100))
        .exec(http("GET /categories/{id}/products")
            .get("/categories/#{newCategoryId}/products")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(200)))
        .pause(Duration.ofMillis(100))
        .exec(http("DELETE /categories/{id}/products/{productId}")
            .delete("/categories/#{newCategoryId}/products/#{newProductId}")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(204)))
        .pause(Duration.ofMillis(100))
        .exec(http("DELETE /products/{id}")
            .delete("/products/#{newProductId}")
            .header("Authorization", "Bearer #{jwt}")
            .check(status().is(204)))
        .pause(Duration.ofMillis(100))
        .exec(http("DELETE /categories/{id}")
                .delete("/categories/#{newCategoryId}")
                .header("Authorization", "Bearer #{jwt}")
                .check(status().is(204)));

    private final ScenarioBuilder scenario = scenario("Scenario").feed(userFeeder)
        .exec(loginChain)
            .pause(Duration.ofMillis(1_000))
        .exec(productChain)
            .pause(Duration.ofMillis(100))
        .exec(categoryChain)
            .pause(Duration.ofMillis(100))
        .exec(subCategoryChain)
            .pause(Duration.ofMillis(100))
        .exec(productCategoryChain);

    {
        setUp(
            scenario.injectOpen(rampUsers(USERS).during(Duration.ofSeconds(RAMP_SECS)))
        ).protocols(httpProtocol);
    }
}