package com.github.damiox.ecommerce.api.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.damiox.ecommerce.api.controller.objects.CredentialsDto;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestBase {
    @LocalServerPort
    private int port;

    protected RestTemplate restTemplate;
    protected ObjectMapper mapper;

    @Before
    public void setUp() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public boolean hasError(HttpStatus statusCode) {
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

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + loginAdmin());
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<String> response = restTemplate.exchange(dbUrl(), HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        System.out.println("!!! DATABASE CLEANUP FINISHED !!!\u001B[0m");
    }

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    protected String loginUrl() {
        return url("/login");
    }

    protected String dbUrl() {
        return url("/admin/db/reset");
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


    protected String loginAdmin() {
        CredentialsDto credentials = new CredentialsDto("admin", "admin");
        return login(credentials);
    }

    protected String loginUser() {
        CredentialsDto credentials = new CredentialsDto("user1", "user1");
        return login(credentials);
    }

    protected String loginUser2() {
        CredentialsDto credentials = new CredentialsDto("user2", "user2");
        return login(credentials);
    }

    protected String login(CredentialsDto credentials) {
        Map<String, String> response = restTemplate.postForObject(loginUrl(), credentials, Map.class);
        return response.get("token").replace("Bearer ", "");
    }

    protected HttpHeaders loginAdminWithHeaders() {
        CredentialsDto credentials = new CredentialsDto("admin", "admin");
        return loginWithHeaders(credentials);
    }

    protected HttpHeaders loginUserWithHeaders() {
        CredentialsDto credentials = new CredentialsDto("user1", "user1");
        return loginWithHeaders(credentials);
    }

    protected HttpHeaders loginUser2WithHeaders() {
        CredentialsDto credentials = new CredentialsDto("user2", "user2");
        return loginWithHeaders(credentials);
    }

    protected HttpHeaders loginWithHeaders(CredentialsDto credentials) {
        Map<String, String> response = restTemplate.postForObject(loginUrl(), credentials, Map.class);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, response.get("token"));
        return headers;
    }
}
