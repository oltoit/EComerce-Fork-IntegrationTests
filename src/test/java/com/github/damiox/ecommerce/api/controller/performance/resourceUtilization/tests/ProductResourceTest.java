package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.tests;

import com.github.damiox.ecommerce.api.controller.objects.ProductDto;
import com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceSampler;
import com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceUtilizationIntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.utils.ProductUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductResourceTest extends ResourceUtilizationIntegrationTestBase {

    @Autowired
    private ProductUtils productUtils;

    @Test
    public void getProducts() {
        // create 1.000 products
        for (int i = 0; i < 1_000; i++) {
            productUtils.createProduct(new ProductDto("test-product-" + i, "EUR", 10.00), user1.id);
        }
        String baseUrl = productsUrl();
        HttpHeaders headers = loginWithHeaders(user1);
        HttpEntity entity = new HttpEntity(headers);

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 600);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getProduct() {
        // create 1.000 products
        long id = productUtils.createProduct(defaultProduct, user1.id);
        for (int i = 0; i < 999; i++) {
            productUtils.createProduct(new ProductDto("test-product-" + i, "EUR", 10.00), user1.id);
        }
        String baseUrl = productUrl((int) id);
        HttpHeaders headers = loginWithHeaders(user1);
        HttpEntity entity = new HttpEntity(headers);

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 2400);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void createProduct() {
        String baseUrl = productsUrl();
        ProductDto product = defaultProduct;
        HttpHeaders headers = loginWithHeaders(user1);
        HttpEntity httpEntity = new HttpEntity(product, headers);

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 2400);
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, httpEntity, Map.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void updateProduct() {
        // create 1.000 products
        long id = productUtils.createProduct(defaultProduct, user1.id);
        for (int i = 0; i < 999; i++) {
            productUtils.createProduct(new ProductDto("test-product-" + i, "EUR", 10.00), user1.id);
        }
        String baseUrl = productUrl((int) id);
        ProductDto product = new ProductDto("updated-product", "EUR", 10.00);
        HttpHeaders headers = loginWithHeaders(user1);
        HttpEntity httpEntity = new HttpEntity(product, headers);

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 2400);
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.PUT, httpEntity, Map.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deleteProduct() {
        // create 1.000 products
        long id = productUtils.createProduct(defaultProduct, user1.id);
        for (int i = 0; i < 999; i++) {
            productUtils.createProduct(new ProductDto("test-product-" + i, "EUR", 10.00), user1.id);
        }
        String baseUrl = productUrl((int) id);
        HttpHeaders headers = loginWithHeaders(user1);
        HttpEntity httpEntity = new HttpEntity(headers);

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 2400);
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.DELETE, httpEntity, Map.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
