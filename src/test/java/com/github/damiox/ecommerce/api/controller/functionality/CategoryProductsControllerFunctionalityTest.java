package com.github.damiox.ecommerce.api.controller.functionality;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.objects.ProductDto;
import com.github.damiox.ecommerce.api.controller.utils.CategoryUtils;
import com.github.damiox.ecommerce.api.controller.utils.ProductCategoryUtils;
import com.github.damiox.ecommerce.api.controller.utils.ProductUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CategoryProductsControllerFunctionalityTest extends IntegrationTestBase {

    private String baseUrl;
    private long categoryId;
    private long notCategoryId;

    @Autowired
    private CategoryUtils categoryUtils;
    @Autowired
    private ProductUtils productUtils;
    @Autowired
    private ProductCategoryUtils productCategoryUtils;

    @Before
    public void init() {
        categoryId = categoryUtils.createCategory("test-category");
        notCategoryId = categoryId + 1;
        baseUrl = categoryProductsUrl(categoryId);
    }

    @Test
    public void getProducts() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getProductsNotLoggedIn() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void getProductsCategoryNotFound() {
        ResponseEntity<String> response = restTemplate.exchange(categoryProductsUrl(notCategoryId), HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void addProduct() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<Map> response = addProduct(loginWithHeaders(user1), categoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertThat(productCategoryUtils.getCategoryIds(productId)).contains(categoryId);
    }

    @Test
    public void addProductAsAdmin() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<Map> response = addProduct(loginWithHeaders(admin), categoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertThat(productCategoryUtils.getCategoryIds(productId)).contains(categoryId);
    }

    @Test
    public void addProductNotLoggedIn() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = addProduct(headers, categoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(productCategoryUtils.getCategoryIds(productId)).doesNotContain(categoryId);
    }

    @Test
    public void addProductWrongUser() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<Map> response = addProduct(loginWithHeaders(user2), categoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(productCategoryUtils.getCategoryIds(productId)).doesNotContain(categoryId);
    }

    @Test
    public void addProductCategoryNotFound() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<Map> response = addProduct(loginWithHeaders(user1), notCategoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void addProductNotFound() {
        ResponseEntity<Map> response = addProduct(loginWithHeaders(user1), categoryId, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void addProductAlreadyAssociated() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = addProduct(loginWithHeaders(user1), categoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void addCategoryToProductAlreadyCategorized() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        long categoryId2 = categoryUtils.createCategory("category-2");
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = addProduct(loginWithHeaders(user1), categoryId2, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertThat(productCategoryUtils.getCategoryIds(productId)).contains(categoryId, categoryId2);
    }

    @Test
    public void removeProduct() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = removeProduct(loginWithHeaders(user1), categoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(productCategoryUtils.getCategoryIds(productId)).doesNotContain(categoryId);
    }

    @Test
    public void removeProductAsAdmin() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = removeProduct(loginWithHeaders(admin), categoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(productCategoryUtils.getCategoryIds(productId)).doesNotContain(categoryId);
    }

    @Test
    public void removeProductNotLoggedIn() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = removeProduct(headers, categoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(productCategoryUtils.getCategoryIds(productId)).contains(categoryId);
    }

    @Test
    public void removeProductWrongUser() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = removeProduct(loginWithHeaders(user2), categoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(productCategoryUtils.getCategoryIds(productId)).contains(categoryId);
    }

    @Test
    public void removeProductCategoryNotFound() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<Map> response = removeProduct(loginWithHeaders(user1), notCategoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void removeProductNotFound() {
        ResponseEntity<Map> response = removeProduct(loginWithHeaders(user1), categoryId, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void removeProductNotAssociated() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<Map> response = removeProduct(loginWithHeaders(user1), categoryId, productId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    // Pagination Tests
    @Test
    public void getProductsPagination() {
        for (int i = 0; i < 3; i++) {
            long productId = productUtils.createProduct(new ProductDto("product-" + i, "EUR", 10.00), user1.id);
            productCategoryUtils.addProductToCategory(productId, categoryId);
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
        for (int i = 0; i < 3; i++) {
            long productId = productUtils.createProduct(new ProductDto("product-" + i, "EUR", 10.00), user1.id);
            productCategoryUtils.addProductToCategory(productId, categoryId);
        }

        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "?page=1&size=2", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(1);
        assertThat(page.get("totalPages")).isEqualTo(2);

        Map embedded = (Map) response.getBody().get("_embedded");
        List<Map> products = (List<Map>) embedded.get("productResourceList");
        assertThat(products).hasSize(1);
    }

    @Test
    public void getProductsPaginationPageOutOfBounds() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "?page=999&size=20", HttpMethod.GET,new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(999);
        assertThat(page.get("totalElements")).isEqualTo(1);
        assertThat(page.get("size")).isEqualTo(20);
    }

    @Test
    public void getProductsPaginationNegativePageIndex() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "?page=-1&size=20", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(0);
        assertThat(page.get("totalElements")).isEqualTo(1);
        assertThat(page.get("size")).isEqualTo(20);
    }

    @Test
    public void getProductsPaginationNegativePageSize() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "?page=0&size=-1", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(0);
        assertThat(page.get("totalElements")).isEqualTo(1);
        assertThat(page.get("size")).isEqualTo(20);
    }

    @Test
    public void getProductsPaginationExceedMaxPageSize() {
        ResponseEntity<Map> response = restTemplate.exchange(baseUrl + "?page=0&size=5000", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("size")).isEqualTo(2000);
    }


    // Private helper functions

    private ResponseEntity<Map> addProduct(HttpHeaders headers, long categoryId, long productId) {
        return restTemplate.exchange(
                categoryProductsUrl(categoryId) + "/" + productId,
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );
    }

    private ResponseEntity<Map> removeProduct(HttpHeaders headers, long categoryId, long productId) {
        return restTemplate.exchange(
                categoryProductsUrl(categoryId) + "/" + productId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Map.class
        );
    }
}