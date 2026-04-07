package com.github.damiox.ecommerce.api.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.damiox.ecommerce.api.controller.objects.CredentialsDto;
import com.github.damiox.ecommerce.api.controller.objects.ProductDto;
import com.github.damiox.ecommerce.api.controller.objects.Role;
import com.github.damiox.ecommerce.api.controller.objects.User;
import com.github.damiox.ecommerce.api.controller.utils.DBAccess;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.sql.SQLException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public abstract class IntegrationTestBase {
    public final int port = 8080;

    protected RestTemplate restTemplate;
    protected ObjectMapper mapper;

    @Autowired
    protected DBAccess db;

    protected static User user1 = new User("user1", "user1", Role.USER, 1);
    protected static User user2 = new User("user2", "user2", Role.USER, 2);
    protected static User admin = new User("admin", "admin", Role.ADMIN, 3);

    protected static ProductDto defaultProduct = new ProductDto("test", "EUR", 10.00);

    @Before
    public void setUp() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public boolean hasError(@NotNull HttpStatusCode statusCode) {
                return false;
            }
        });
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // LOG entries are colored in cyan so they can be distinguished from other LOG entries
    @After
    public void cleanDB() {
        System.out.println("\u001B[36m!!! DATABASE CLEANUP INITIALIZED !!!");

        try {
            db.resetDb();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        System.out.println("!!! DATABASE CLEANUP FINISHED !!!\u001B[0m");
    }

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    protected String loginUrl() {
        return url("/login");
    }

    protected String productsUrl() {
        return url("/products");
    }

    protected String productUrl(int index) {
        return productsUrl() + "/" + index;
    }

    protected String categoriesUrl() {
        return url("/categories");
    }

    protected String categoryUrl(int index) {
        return categoriesUrl() + "/" + index;
    }

    protected String subcategoriesUrl(long parentId) {
        return categoryUrl((int) parentId) + "/subcategories";
    }

    protected String subcategoryUrl(long parentId, long childId) {
        return subcategoriesUrl(parentId) + "/" + childId;
    }

    protected String categoryProductsUrl(long categoryId) {
        return categoriesUrl() + "/" + categoryId + "/products";
    }

    protected String categoryProductUrl(long categoryId, long productId) {
        return categoryProductsUrl(categoryId) + "/" + productId;
    }

    protected String login(User user) {
        CredentialsDto credentials = new CredentialsDto(user.name, user.password);
        Map<String, String> response = restTemplate.postForObject(loginUrl(), credentials, Map.class);
        return response.get("token").replace("Bearer ", "");
    }

    protected HttpHeaders loginWithHeaders(User user) {
        CredentialsDto credentials = new CredentialsDto(user.name, user.password);
        Map<String, String> response = restTemplate.postForObject(loginUrl(), credentials, Map.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, response.get("token"));
        return headers;
    }
}
