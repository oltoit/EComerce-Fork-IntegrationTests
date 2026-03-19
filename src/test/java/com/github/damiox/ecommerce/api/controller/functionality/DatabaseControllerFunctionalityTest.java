package com.github.damiox.ecommerce.api.controller.functionality;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.objects.ProductDto;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.*;

import java.util.Map;

import static com.github.damiox.ecommerce.api.controller.utils.ProductUtils.createDefaultProduct;
import static com.github.damiox.ecommerce.api.controller.utils.ProductUtils.getLink;
import static org.assertj.core.api.Assertions.assertThat;

// Test-coverage according to jacoco is only 41% on DatabaseController -> try-catch block on Database Controller not tested
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabaseControllerFunctionalityTest extends IntegrationTestBase {

    private String baseUrl;
    private String adminToken;

    @Before
    public void init() {
        baseUrl = dbUrl();
        adminToken = loginAdmin();
    }

    @Test
    public void resetDBNotLoggedIn() {
        ResponseEntity<String> entity = restTemplate.exchange(baseUrl, HttpMethod.POST, null, String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void resetDBLoggedInAsNormalUser() {
        String token = loginUser();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        HttpEntity httpEntity = new HttpEntity(headers);

        ResponseEntity<String> entity = restTemplate.exchange(baseUrl, HttpMethod.POST, httpEntity, String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void resetDB() {
        // create object
        HttpHeaders headers = loginAdminWithHeaders();
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, productsUrl(), restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // reset DB
        HttpEntity headerEntity = new HttpEntity(headers);
        ResponseEntity<String> emtpyEntity = restTemplate.exchange(baseUrl, HttpMethod.POST, headerEntity, String.class);
        assertThat(emtpyEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // check if entity really doesn't exist
        String entityUrl = getLink(productEntity);
        ResponseEntity<Map> productNotFoundEntity = restTemplate.exchange(entityUrl, HttpMethod.GET, headerEntity, Map.class);
        assertThat(productNotFoundEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
