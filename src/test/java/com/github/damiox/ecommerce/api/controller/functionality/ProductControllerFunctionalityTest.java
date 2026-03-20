package com.github.damiox.ecommerce.api.controller.functionality;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.objects.ProductDto;
import com.github.damiox.ecommerce.api.controller.utils.ProductUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.*;

import java.util.Map;

import static com.github.damiox.ecommerce.api.controller.utils.ProductUtils.assertEquality;
import static com.github.damiox.ecommerce.api.controller.utils.ProductUtils.createDefaultProduct;
import static com.github.damiox.ecommerce.api.controller.utils.ProductUtils.createProduct;
import static com.github.damiox.ecommerce.api.controller.utils.ProductUtils.getLink;
import static com.github.damiox.ecommerce.api.controller.utils.ProductUtils.updateDefaultProduct;
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
    public void createProductInUsd() {
        // create object
        HttpHeaders headers = loginUserWithHeaders();
        ProductDto product = new ProductDto("usd-product", "USD", 10.00);
        HttpEntity httpEntity = new HttpEntity(headers);
        ResponseEntity<Map> productEntity = createProduct(headers, product, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // get object
        ResponseEntity<Map> response = restTemplate.exchange(getLink(productEntity), HttpMethod.GET, httpEntity, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // compare to original
        // use assertion that USD is static factor of 1.146427
        product.setPrice(Math.round(product.getPrice() * (1.0 / 1.146427) * 100.0) / 100.0);
        product.setCurrency("EUR");

        ProductDto responseProduct = mapper.convertValue(response.getBody(), ProductDto.class);
        assertThat(product).isEqualTo(responseProduct);
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
    public void updateProduct() {
        // create first product
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // create second product on first products path
        String product1Url = getLink(productEntity);
        ResponseEntity<Map> product2Entity = updateDefaultProduct(headers, product1Url, restTemplate);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.OK);

        // get product
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Map> response = restTemplate.exchange(getLink(productEntity), HttpMethod.GET, entity, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // compare product before and after update
        assertThat(assertEquality(productEntity, response, mapper)).isFalse();
    }

    @Test
    public void updateProductNotLoggedIn() {
        // create first product
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // update first product
        String product1Url = getLink(productEntity);
        HttpHeaders headers2 = new HttpHeaders();
        headers2.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> product2Entity = updateDefaultProduct(headers2, product1Url, restTemplate);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // get product
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Map> response = restTemplate.exchange(getLink(productEntity), HttpMethod.GET, entity, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // compare product before and after update
        assertThat(assertEquality(productEntity, response, mapper)).isTrue();
    }

    @Test
    public void updateProductWrongUser() {
        // create first product
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // create second product on first products path
        String product1Url = getLink(productEntity);
        HttpHeaders headers2 = loginUser2WithHeaders();
        ResponseEntity<Map> product2Entity = updateDefaultProduct(headers2, product1Url, restTemplate);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // get product
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Map> response = restTemplate.exchange(getLink(productEntity), HttpMethod.GET, entity, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // compare product before and after update
        assertThat(assertEquality(productEntity, response, mapper)).isTrue();
    }

    @Test
    public void updateProductNotFound() {
        // create second product on first products path
        String product1Url = baseUrl + "/1";
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<Map> productEntity = updateDefaultProduct(headers, product1Url, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateProductWrongCurrency() {
        // create first product
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // create second product on first products path
        String product1Url = getLink(productEntity);
        ProductDto productWrongCurrency = new ProductDto("wrong-currency-product", "BITCOIN", 10000.0);
        ResponseEntity<Map> product2Entity = ProductUtils.updateProduct(headers, productWrongCurrency, product1Url, restTemplate);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // get product
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<Map> response = restTemplate.exchange(getLink(productEntity), HttpMethod.GET, entity, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // compare product before and after update
        assertThat(assertEquality(productEntity, response, mapper)).isTrue();
    }

    @Test
    public void deleteProduct() {
        // create first product
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // create second product on first products path
        String productUrl = getLink(productEntity);
        ResponseEntity<Map> product2Entity = ProductUtils.deleteProduct(headers, productUrl, restTemplate);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // get product
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(productUrl, HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteProductNonExistent() {
        // create second product on first products path
        HttpHeaders headers = loginUserWithHeaders();
        String productUrl = baseUrl + "/1";
        ResponseEntity<Map> product2Entity = ProductUtils.deleteProduct(headers, productUrl, restTemplate);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // get product
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(productUrl, HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteProductAsAdmin() {
        // create first product
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // create second product on first products path
        String productUrl = getLink(productEntity);
        HttpHeaders headers2 = loginAdminWithHeaders();
        ResponseEntity<Map> product2Entity = ProductUtils.deleteProduct(headers2, productUrl, restTemplate);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // get product
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(productUrl, HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void deleteProductNotLoggedIn() {
        // create first product
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // create second product on first products path
        String productUrl = getLink(productEntity);
        HttpHeaders headers2 = new HttpHeaders();
        headers2.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> product2Entity = ProductUtils.deleteProduct(headers2, productUrl, restTemplate);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // get product
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(productUrl, HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deleteProductWrongUser() {
        // create first product
        HttpHeaders headers = loginUserWithHeaders();
        ResponseEntity<Map> productEntity = createDefaultProduct(headers, baseUrl, restTemplate);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // create second product on first products path
        String productUrl = getLink(productEntity);
        HttpHeaders headers2 = loginUser2WithHeaders();
        ResponseEntity<Map> product2Entity = ProductUtils.deleteProduct(headers2, productUrl, restTemplate);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // get product
        HttpEntity entity = new HttpEntity(headers);
        ResponseEntity<String> response = restTemplate.exchange(productUrl, HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
