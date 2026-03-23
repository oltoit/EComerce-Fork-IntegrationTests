package com.github.damiox.ecommerce.api.controller.functionality;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.objects.ProductDto;
import com.github.damiox.ecommerce.api.controller.utils.ProductUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProductControllerFunctionalityTest extends IntegrationTestBase {
    private String baseUrl;

    @Autowired
    private ProductUtils productUtils;

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
        HttpHeaders headers = loginWithHeaders(user1);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getProductNotFound() {
        HttpHeaders headers = loginWithHeaders(user1);
        HttpEntity entity = new HttpEntity(headers);

        String product1Url = productUrl(1);

        ResponseEntity<String> response = restTemplate.exchange(product1Url, HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getProduct() {
        // create object
        long id = productUtils.createProduct(defaultProduct, user1.id);
        String productUrl = baseUrl + "/" + id;

        // get object
        ResponseEntity<String> response = restTemplate.exchange(productUrl, HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void createProduct() throws SQLException {
        // create object
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST,new HttpEntity<>(defaultProduct, loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        long id = productUtils.getId(response);

        // check that object exists
        ProductDto product = productUtils.getProduct(id);
        assertThat(product).isNotNull();
        assertThat(product).isEqualTo(defaultProduct);
    }

    @Test
    public void createProductInUsd() throws SQLException{
        ProductDto product = new ProductDto("usd-product", "USD", 10.00);

        // create product
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl, HttpMethod.POST, new HttpEntity<>(product, loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // get product
        ProductDto productFromDb = productUtils.getProduct(productUtils.getId(response));

        product.setPrice(Math.round(product.getPrice() * (1.0 / 1.146427) * 100.0) / 100.0);
        product.setCurrency("EUR");

        assertThat(product).isEqualTo(productFromDb);
    }

    @Test
    public void createProductNotLoggedIn() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> productEntity = createDefaultProduct();
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void createProductWrongCurrency() {
        HttpHeaders headers = loginWithHeaders(user1);
        ProductDto product = new ProductDto("wrong-currency-product", "BITCOIN", 10000.0);

        ResponseEntity<Map> productEntity = createProduct(headers, product);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createProductAlreadyAvailable() {
        // create first product
        long id = productUtils.createProduct(defaultProduct, user1.id);

        // create second product on first products path
        ResponseEntity<Map> productEntity = createProduct(productUrl((int) id), loginWithHeaders(user1), defaultProduct);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    public void updateProduct() throws SQLException {
        // create first product
        long id = productUtils.createProduct(defaultProduct, user1.id);

        // update second product on first products path
        HttpHeaders headers = loginWithHeaders(user1);
        String productUrl = productUrl((int) id);
        ResponseEntity<Map> productEntity = updateDefaultProduct(headers, productUrl);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        // get product
        ProductDto productFromDb = productUtils.getProduct(id);
        assertThat(defaultProduct).isNotEqualTo(productFromDb);
    }

    @Test
    public void updateProductNotLoggedIn() throws SQLException{
        // create first product
        long id = productUtils.createProduct(defaultProduct, user1.id);

        // update second product on first products path
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String productUrl = productUrl((int) id);
        ResponseEntity<Map> productEntity = updateDefaultProduct(headers, productUrl);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // get product
        ProductDto productFromDb = productUtils.getProduct(id);
        assertThat(defaultProduct).isEqualTo(productFromDb);
    }

    @Test
    public void updateProductWrongUser() throws SQLException{
        // create first product
        long id = productUtils.createProduct(defaultProduct, user1.id);

        // update second product on first products path
        HttpHeaders headers = loginWithHeaders(user2);
        String productUrl = productUrl((int) id);
        ResponseEntity<Map> productEntity = updateDefaultProduct(headers, productUrl);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // get product
        ProductDto productFromDb = productUtils.getProduct(id);
        assertThat(defaultProduct).isEqualTo(productFromDb);
    }

    @Test
    public void updateProductNotFound() throws SQLException{
        // create second product on first products path
        long id = 1;
        HttpHeaders headers = loginWithHeaders(user1);
        ResponseEntity<Map> productEntity = updateDefaultProduct(headers, productUrl((int) 1));
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // assert that no object is found
        assertThatThrownBy(() -> productUtils.getProduct(id)).isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    public void updateProductWrongCurrency() throws SQLException {
        // create first product
        long id = productUtils.createProduct(defaultProduct, user1.id);

        // create second product on first products path
        String productUrl = productUrl((int) id);
        HttpHeaders headers = loginWithHeaders(user1);
        ProductDto productWrongCurrency = new ProductDto("wrong-currency-product", "BITCOIN", 10000.0);
        ResponseEntity<Map> productEntity = updateProduct(headers, productWrongCurrency, productUrl);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // get product
        ProductDto product = productUtils.getProduct((int) id);
        assertThat(product).isEqualTo(defaultProduct);
    }

    @Test
    public void deleteProduct() {
        // create first product
        long id = productUtils.createProduct(defaultProduct, user1.id);

        // create second product on first products path
        String productUrl = productUrl((int) id);
        HttpHeaders headers = loginWithHeaders(user1);
        ResponseEntity<Map> product2Entity = deleteProduct(headers, productUrl);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert that no object is found
        assertThatThrownBy(() -> productUtils.getProduct(id)).isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    public void deleteProductNonExistent() {
        long id = 1;

        HttpHeaders headers = loginWithHeaders(user1);
        String productUrl = productUrl((int) id);
        ResponseEntity<Map> product2Entity = deleteProduct(headers, productUrl);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        // assert that no object is found
        assertThatThrownBy(() -> productUtils.getProduct(id)).isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    public void deleteProductAsAdmin() {
        // create first product
        long id = productUtils.createProduct(defaultProduct, user1.id);

        // create second product on first products path
        String productUrl = productUrl((int) id);
        HttpHeaders headers = loginWithHeaders(admin);
        ResponseEntity<Map> product2Entity = deleteProduct(headers, productUrl);
        assertThat(product2Entity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // assert that no object is found
        assertThatThrownBy(() -> productUtils.getProduct(id)).isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    public void deleteProductNotLoggedIn() throws SQLException{
        // create first product
        long id = productUtils.createProduct(defaultProduct, user1.id);

        // create second product on first products path
        String productUrl = productUrl((int) id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> productEntity = deleteProduct(headers, productUrl);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // get product
        ProductDto product = productUtils.getProduct(id);
        assertThat(product).isEqualTo(defaultProduct);
    }

    @Test
    public void deleteProductWrongUser() throws SQLException{
        // create first product
        long id = productUtils.createProduct(defaultProduct, user1.id);

        // create second product on first products path
        String productUrl = productUrl((int) id);
        HttpHeaders headers = loginWithHeaders(user2);
        ResponseEntity<Map> productEntity = deleteProduct(headers, productUrl);
        assertThat(productEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // get product
        ProductDto product = productUtils.getProduct(id);
        assertThat(product).isEqualTo(defaultProduct);
    }

    // Page Tests
    @Test
    public void getProductsPagination() {
        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            productUtils.createProduct(new ProductDto(name, "EUR", 10.00), user1.id);
        }

        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "?page=0&size=2", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("size")).isEqualTo(2);
        assertThat(page.get("totalElements")).isEqualTo(3);
        assertThat(page.get("totalPages")).isEqualTo(2);
        assertThat(page.get("number")).isEqualTo(0);
    }

    @Test
    public void getProductsPaginationPage2() {
        // create 3 products
        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            productUtils.createProduct(new ProductDto(name, "EUR", 10.00), user1.id);
        }

        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "?page=1&size=2", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(1);
        assertThat(page.get("totalPages")).isEqualTo(2);

        // page 2 should only have 1 element
        Map embedded = (Map) response.getBody().get("_embedded");
        List<Map> products = (List<Map>) embedded.get("productResourceList");
        assertThat(products).hasSize(1);
    }

    @Test
    public void getProductsPaginationPageOutOfBounds() {
        productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "?page=999&size=20", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(999);
        assertThat(page.get("totalElements")).isEqualTo(1);
    }

    @Test
    public void getProductsPaginationNegativePageIndex() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "?page=-1&size=20", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getProductsPaginationNegativePageSize() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "?page=0&size=-1", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getProductsPaginationMaxPageSize() {
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "?page=0&size=5000", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        // default max-page size is 2000 -> assert that even with pagesize of 5000 in query 2000 will be actual page size
        assertThat(page.get("size")).isEqualTo(2000);
    }

    @Test
    public void getProductsPaginationExceedMaxPageSize() {
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "?page=0&size=2001", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Spring caps the size at 2000 instead of returning an error
        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("size")).isEqualTo(2000);
    }


    // Private helper functions

    private ResponseEntity<Map> createProduct(HttpHeaders headers, ProductDto product) {
        return createProduct(baseUrl, headers, product);
    }

    private ResponseEntity<Map> createProduct(String url, HttpHeaders headers, ProductDto product) {
        HttpEntity httpEntity = new HttpEntity(product, headers);
        return restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);
    }

    private ResponseEntity<Map> createDefaultProduct() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(baseUrl, HttpMethod.POST, new HttpEntity<>(productsUrl(), headers), Map.class);
    }

    private ResponseEntity<Map> updateDefaultProduct(HttpHeaders headers, String url) {
        ProductDto product = new ProductDto("updated", "EUR", 12.00);
        return updateProduct(headers, product, url);
    }

    private ResponseEntity<Map> updateProduct(HttpHeaders headers, ProductDto product, String url) {
        HttpEntity httpEntity = new HttpEntity(product, headers);
        return restTemplate.exchange(url, HttpMethod.PUT, httpEntity, Map.class);
    }

    private ResponseEntity<Map> deleteProduct(HttpHeaders headers, String url) {
        return restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(headers), Map.class);
    }
}
