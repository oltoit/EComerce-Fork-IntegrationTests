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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
Tests for testing that pagination works correctly.
They have been separated from controller-specific tests so they are shorter and more readable.
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PaginationFunctionalityTests extends IntegrationTestBase {

    private String productBaseUrl;
    private String categoryProductsBaseUrl;
    private long categoryId;

    @Autowired
    private CategoryUtils categoryUtils;
    @Autowired
    private ProductUtils productUtils;
    @Autowired
    private ProductCategoryUtils productCategoryUtils;

    @Before
    public void init() {
        productBaseUrl = productsUrl();
        categoryId = categoryUtils.createCategory("test-category");
        categoryProductsBaseUrl = categoryProductsUrl(categoryId);
    }

    // ProductController Pagination Tests
    @Test
    public void getProductsPagination() {
        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            productUtils.createProduct(new ProductDto(name, "EUR", 10.00), user1.id);
        }

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?page=0&size=2", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
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
            String name = "product-"  + i;
            productUtils.createProduct(new ProductDto(name, "EUR", 10.00), user1.id);
        }

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?page=1&size=2", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
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

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?page=999&size=20", HttpMethod.GET,new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(999);
        assertThat(page.get("totalElements")).isEqualTo(1);
        assertThat(page.get("size")).isEqualTo(20);
    }

    @Test
    public void getProductsPaginationNegativePageIndex() {
        productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?page=-1&size=20", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(0);
        assertThat(page.get("totalElements")).isEqualTo(1);
        assertThat(page.get("size")).isEqualTo(20);
    }

    @Test
    public void getProductsPaginationNegativePageSize() {
        productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?page=0&size=-1", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(0);
        assertThat(page.get("totalElements")).isEqualTo(1);
        assertThat(page.get("size")).isEqualTo(20);
    }

    @Test
    public void getProductsPaginationPageSizeZero() {
        productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?page=0&size=0", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(0);
        assertThat(page.get("totalElements")).isEqualTo(1);
        assertThat(page.get("size")).isEqualTo(20);
    }

    @Test
    public void getProductsPaginationExceedMaxPageSize() {
        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?page=0&size=5000", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Spring caps the size at 2000 instead of returning an error
        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("size")).isEqualTo(2000);
    }


    // CategoryProductsController Pagination Tests
    @Test
    public void getCategoryProductsPagination() {
        for (int i = 0; i < 3; i++) {
            long productId = productUtils.createProduct(new ProductDto("product-" + i, "EUR", 10.00), user1.id);
            productCategoryUtils.addProductToCategory(productId, categoryId);
        }

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?page=0&size=2", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("size")).isEqualTo(2);
        assertThat(page.get("totalElements")).isEqualTo(3);
        assertThat(page.get("totalPages")).isEqualTo(2);
        assertThat(page.get("number")).isEqualTo(0);
    }

    @Test
    public void getCategoryProductsPaginationPage2() {
        for (int i = 0; i < 3; i++) {
            long productId = productUtils.createProduct(new ProductDto("product-" + i, "EUR", 10.00), user1.id);
            productCategoryUtils.addProductToCategory(productId, categoryId);
        }

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?page=1&size=2", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(1);
        assertThat(page.get("totalPages")).isEqualTo(2);

        Map embedded = (Map) response.getBody().get("_embedded");
        List<Map> products = (List<Map>) embedded.get("productResourceList");
        assertThat(products).hasSize(1);
    }

    @Test
    public void getCategoryProductsPaginationPageOutOfBounds() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?page=999&size=20", HttpMethod.GET,new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(999);
        assertThat(page.get("totalElements")).isEqualTo(1);
        assertThat(page.get("size")).isEqualTo(20);
    }

    @Test
    public void getCategoryProductsPaginationNegativePageIndex() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?page=-1&size=20", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(0);
        assertThat(page.get("totalElements")).isEqualTo(1);
        assertThat(page.get("size")).isEqualTo(20);
    }

    @Test
    public void getCategoryProductsPaginationNegativePageSize() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?page=0&size=-1", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(0);
        assertThat(page.get("totalElements")).isEqualTo(1);
        assertThat(page.get("size")).isEqualTo(20);
    }

    @Test
    public void getCategoryProductsPaginationPageSizeZero() {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?page=0&size=0", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("number")).isEqualTo(0);
        assertThat(page.get("totalElements")).isEqualTo(1);
        assertThat(page.get("size")).isEqualTo(20);
    }

    @Test
    public void getCategoryProductsPaginationExceedMaxPageSize() {
        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?page=0&size=5000", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map page = (Map) response.getBody().get("page");
        assertThat(page.get("size")).isEqualTo(2000);
    }


    // TODO: expand these tests for the getProductsForCategory endpoints as well
    /* Tests for sorting of pagination */

    @Test
    public void getProductsPaginationSortByName() {
        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            productUtils.createProduct(new ProductDto(name, "EUR", 10.00), user1.id);
        }

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?sort=name", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            Map product = (Map) products.get(i);
            assertThat(product.get("name")).isEqualTo(name);
        }
    }

    @Test
    public void getProductsPaginationSortByNameAscending() {
        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            productUtils.createProduct(new ProductDto(name, "EUR", 10.00), user1.id);
        }

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?sort=name,asc", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            Map product = (Map) products.get(i);
            assertThat(product.get("name")).isEqualTo(name);
        }
    }

    @Test
    public void getProductsPaginationSortByNameDescending() {
        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            productUtils.createProduct(new ProductDto(name, "EUR", 10.00), user1.id);
        }

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?sort=name,desc", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        int j = 0;
        for (int i = 2; i >= 0; i--) {
            String name = "product-"  + i;
            Map product = (Map) products.get(j);
            assertThat(product.get("name")).isEqualTo(name);
            j++;
        }
    }

    @Test
    public void getProductsPaginationSortById() {
        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            productUtils.createProduct(new ProductDto(name, "EUR", 10.00), user1.id);
        }

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?sort=id", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            Map product = (Map) products.get(i);
            assertThat(product.get("name")).isEqualTo(name);
        }
    }

    @Test
    public void getProductsPaginationSortByIdAsc() {
        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            productUtils.createProduct(new ProductDto(name, "EUR", 10.00), user1.id);
        }

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?sort=id,asc", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            Map product = (Map) products.get(i);
            assertThat(product.get("name")).isEqualTo(name);
        }
    }

    @Test
    public void getProductsPaginationSortByIdDesc() {
        for (int i = 0; i < 3; i++) {
            String name = "product-"  + i;
            productUtils.createProduct(new ProductDto(name, "EUR", 10.00), user1.id);
        }

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?sort=id,desc", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        int j = 0;
        for (int i = 2; i >= 0; i--) {
            String name = "product-"  + i;
            Map product = (Map) products.get(j);
            assertThat(product.get("name")).isEqualTo(name);
            j++;
        }
    }

    @Test
    public void getProductsPaginationSortByUserAndPrice() {
        productUtils.createProduct(new ProductDto("product-100", "EUR", 2000), user2.id);
        productUtils.createProduct(new ProductDto("product-1", "EUR", 10), user1.id);
        productUtils.createProduct(new ProductDto("product-2", "EUR", 5), user1.id);
        productUtils.createProduct(new ProductDto("product-3", "EUR", 20), user1.id);

        ResponseEntity<Map> response = restTemplate.exchange(productBaseUrl + "?sort=user,desc&sort=price,asc", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        assertThat(((Map)products.get(0)).get("name")).isEqualTo("product-100");
        assertThat(((Map)products.get(1)).get("name")).isEqualTo("product-2");
        assertThat(((Map)products.get(2)).get("name")).isEqualTo("product-1");
        assertThat(((Map)products.get(3)).get("name")).isEqualTo("product-3");
    }

    @Test
    public void getCategoryProductsPaginationSortByName() {
        for (int i = 0; i < 3; i++) {
            long productId = productUtils.createProduct(new ProductDto("product-" + i, "EUR", 10.00), user1.id);
            productCategoryUtils.addProductToCategory(productId, categoryId);
        }

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?sort=name", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        for (int i = 0; i < 3; i++) {
            assertThat(((Map)products.get(i)).get("name")).isEqualTo("product-" + i);
        }
    }

    @Test
    public void getCategoryProductsPaginationSortByNameAscending() {
        for (int i = 0; i < 3; i++) {
            long productId = productUtils.createProduct(new ProductDto("product-" + i, "EUR", 10.00), user1.id);
            productCategoryUtils.addProductToCategory(productId, categoryId);
        }

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?sort=name,asc", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        for (int i = 0; i < 3; i++) {
            assertThat(((Map)products.get(i)).get("name")).isEqualTo("product-" + i);
        }
    }

    @Test
    public void getCategoryProductsPaginationSortByNameDescending() {
        for (int i = 0; i < 3; i++) {
            long productId = productUtils.createProduct(new ProductDto("product-" + i, "EUR", 10.00), user1.id);
            productCategoryUtils.addProductToCategory(productId, categoryId);
        }

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?sort=name,desc", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        int j = 2;
        for (int i = 0; i < 3; i++) {
            assertThat(((Map)products.get(i)).get("name")).isEqualTo("product-" + j);
            j--;
        }
    }

    @Test
    public void getCategoryProductsPaginationSortByPrice() {
        for (int i = 0; i < 3; i++) {
            long productId = productUtils.createProduct(new ProductDto("product-" + i, "EUR", 10.00 - i), user1.id);
            productCategoryUtils.addProductToCategory(productId, categoryId);
        }

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?sort=price", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        int j = 2;
        for (int i = 0; i < 3; i++) {
            assertThat(((Map)products.get(i)).get("name")).isEqualTo("product-" + j);
            j--;
        }
    }

    @Test
    public void getCategoryProductsPaginationSortByPriceAscending() {
        for (int i = 0; i < 3; i++) {
            long productId = productUtils.createProduct(new ProductDto("product-" + i, "EUR", 10.00 - i), user1.id);
            productCategoryUtils.addProductToCategory(productId, categoryId);
        }

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?sort=price,asc", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        int j = 2;
        for (int i = 0; i < 3; i++) {
            assertThat(((Map)products.get(i)).get("name")).isEqualTo("product-" + j);
            j--;
        }
    }

    @Test
    public void getCategoryProductsPaginationSortByPriceDescending() {
        for (int i = 0; i < 3; i++) {
            long productId = productUtils.createProduct(new ProductDto("product-" + i, "EUR", 10.00 - i), user1.id);
            productCategoryUtils.addProductToCategory(productId, categoryId);
        }

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?sort=price,desc", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        for (int i = 0; i < 3; i++) {
            assertThat(((Map)products.get(i)).get("name")).isEqualTo("product-" + i);
        }
    }

    @Test
    public void getCategoryProductsPaginationSortByUserAndPrice() {
        var product1 = productUtils.createProduct(new ProductDto("product-1", "EUR", 10), user1.id);
        var product2 = productUtils.createProduct(new ProductDto("product-2", "EUR", 5), user1.id);
        var product3 = productUtils.createProduct(new ProductDto("product-3", "EUR", 20), user1.id);
        var product4 = productUtils.createProduct(new ProductDto("product-4", "EUR", 200), user2.id);
        productCategoryUtils.addProductToCategory(product1, categoryId);
        productCategoryUtils.addProductToCategory(product2, categoryId);
        productCategoryUtils.addProductToCategory(product3, categoryId);
        productCategoryUtils.addProductToCategory(product4, categoryId);

        ResponseEntity<Map> response = restTemplate.exchange(categoryProductsBaseUrl + "?sort=userid,desc&sort=price,asc", HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map embedded = (Map) response.getBody().get("_embedded");
        List products = (List) embedded.get("productResourceList");

        assertThat(((Map)products.get(0)).get("name")).isEqualTo("product-4");
        assertThat(((Map)products.get(1)).get("name")).isEqualTo("product-2");
        assertThat(((Map)products.get(2)).get("name")).isEqualTo("product-1");
        assertThat(((Map)products.get(3)).get("name")).isEqualTo("product-3");
    }
}
