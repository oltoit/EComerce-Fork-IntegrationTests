package com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.tests;

import com.github.damiox.ecommerce.api.controller.objects.ProductDto;
import com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.PerfTimer;
import com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.TimeBehaviourIntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.utils.CategoryUtils;
import com.github.damiox.ecommerce.api.controller.utils.ProductCategoryUtils;
import com.github.damiox.ecommerce.api.controller.utils.ProductUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CategoryProductsPerfTimeTest extends TimeBehaviourIntegrationTestBase {
    @Autowired
    private CategoryUtils categoryUtils;
    @Autowired
    private ProductUtils productUtils;
    @Autowired
    private ProductCategoryUtils productCategoryUtils;

    @Test
    public void getCategoryProducts() {
        long categoryId = categoryUtils.createCategory("test");
        for (int i = 0; i < 1_000; i++) {
            long id = productUtils.createProduct(new ProductDto("test-" + i, "EUR", 10.00), user1.id);
            productCategoryUtils.addProductToCategory(id, categoryId);
        }

        String url = categoryProductsUrl(categoryId);
        HttpEntity httpEntity = new HttpEntity(loginWithHeaders(user1));

        PerfTimer.start();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        PerfTimer.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void addProductToCategory() {
        long categoryId = categoryUtils.createCategory("test");
        for (int i = 0; i < 1_000; i++) {
            long id = productUtils.createProduct(new ProductDto("test-" + i, "EUR", 10.00), user1.id);
            productCategoryUtils.addProductToCategory(id, categoryId);
        }
        long productId = productUtils.createProduct(defaultProduct, user1.id);

        String url = categoryProductUrl(categoryId, productId);
        HttpEntity httpEntity = new HttpEntity(loginWithHeaders(user1));

        PerfTimer.start();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        PerfTimer.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void removeProductFromCategory() {
        long categoryId = categoryUtils.createCategory("test");
        for (int i = 0; i < 1_000; i++) {
            long id = productUtils.createProduct(new ProductDto("test-" + i, "EUR", 10.00), user1.id);
            productCategoryUtils.addProductToCategory(id, categoryId);
        }
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        productCategoryUtils.addProductToCategory(productId, categoryId);

        String url = categoryProductUrl(categoryId, productId);
        HttpEntity httpEntity = new HttpEntity(loginWithHeaders(user1));

        PerfTimer.start();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, String.class);
        PerfTimer.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
