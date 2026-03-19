package com.github.damiox.ecommerce.api.controller.functionality;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.*;

import java.util.Map;

import static com.github.damiox.ecommerce.api.controller.utils.ProductUtils.createDefaultProduct;
import static com.github.damiox.ecommerce.api.controller.utils.ProductUtils.getLink;
import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductControllerFunctionalityTest extends IntegrationTestBase {
    private String baseUrl;

    @Before
    public void init() {
        baseUrl = productsUrl();
    }

    @Test
    public void getProductsNotLoggedIn() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void getProducts() {
        HttpHeaders headers = loginUserWithHeaders();
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getProductNotFound() {
        HttpHeaders headers = loginUserWithHeaders();
        HttpEntity entity = new HttpEntity(headers);

        String product1Url = productUrl(1);

        ResponseEntity<String> response = restTemplate.exchange(product1Url, HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getProduct() {
        // create object
        HttpHeaders headers = loginUserWithHeaders();
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // get object
        ResponseEntity<String> response = restTemplate.exchange(getLink(productEntity), HttpMethod.GET, httpEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
