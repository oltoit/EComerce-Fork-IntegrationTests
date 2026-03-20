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
import static com.github.damiox.ecommerce.api.controller.utils.ProductUtils.createProduct;
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

    // This also tests the happy path of create product
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

    @Test
    public void createProductNotLoggedIn() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void createProductWrongCurrency() {
        HttpHeaders headers = loginUserWithHeaders();
        ProductDto product = new ProductDto("wrong-currency-product", "BITCOIN", 10000.0);
        ResponseEntity<Map> productEntity = createProduct(headers, product, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createProductAlreadyAvailable() {
        // create first product
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // create second product on first products path
        String product1Url = getLink(productEntity);
        ResponseEntity<Map> product2Entity = createDefaultProduct(headers, product1Url, restTemplate);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    public void createProductAlreadyAvailableOtherUser() {
        // create first product
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // create second product on first products path
        String product1Url = getLink(productEntity);
        HttpHeaders headers2 = loginUser2WithHeaders();
        ResponseEntity<Map> product2Entity = createDefaultProduct(headers2, product1Url, restTemplate);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
