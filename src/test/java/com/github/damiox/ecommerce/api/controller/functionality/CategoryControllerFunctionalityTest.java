package com.github.damiox.ecommerce.api.controller.functionality;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

// TODO: tests schreiben
// TODO: beachten auch tests für Category-mäßige Methoden im ProduktController getestet werden
public class CategoryControllerFunctionalityTest extends IntegrationTestBase {

    private String baseUrl;

    @Before
    public void init() {
        baseUrl = categoriesUrl();
    }

    @Test
    public void getAllCategories() {
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getAllCategoriesNotLoggedIn() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void getCategory() {

    }
}
