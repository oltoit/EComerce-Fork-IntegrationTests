package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.tests;

import com.github.damiox.ecommerce.api.controller.objects.CategoryDto;
import com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceSampler;
import com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceUtilizationIntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.utils.CategoryUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CategoryResourceTest extends ResourceUtilizationIntegrationTestBase {

    @Autowired
    private CategoryUtils categoryUtils;

    @Test
    public void getAllCategories() {
        // create 1.000 categories
        for (int i = 0; i < 1_000; i++) {
            categoryUtils.createCategory("test-category-" + i);
        }
        HttpHeaders headers = loginWithHeaders(user1);
        HttpEntity httpEntity = new HttpEntity(headers);
        String categoriesUrl = categoriesUrl();

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 30);
        ResponseEntity<String> response = restTemplate.exchange(categoriesUrl, HttpMethod.GET, httpEntity, String.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getCategory() {
        // create 1.000 categories
        long id = categoryUtils.createCategory("the-chosen-one");
        for (int i = 0; i < 999; i++) {
            categoryUtils.createCategory("test-category-" + i);
        }
        HttpHeaders headers = loginWithHeaders(user1);
        HttpEntity httpEntity = new HttpEntity(headers);
        String categoriyUrl = categoryUrl((int) id);

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 100);
        ResponseEntity<String> response = restTemplate.exchange(categoriyUrl, HttpMethod.GET, httpEntity, String.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Create category
    @Test
    public void createCategory() {
        String baseUrl = categoriesUrl();
        CategoryDto category = new CategoryDto("test-category");
        HttpHeaders headers = loginWithHeaders(admin);
        HttpEntity httpEntity = new HttpEntity(category, headers);

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 100);
        ResponseEntity<String> response =  restTemplate.exchange(baseUrl, HttpMethod.POST, httpEntity, String.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    // Update category
    @Test
    public void updateCategory() {
        // create 1.000 categories
        long id = categoryUtils.createCategory("the-chosen-one");
        for (int i = 0; i < 999; i++) {
            categoryUtils.createCategory("test-category-" + i);
        }

        String baseUrl = categoryUrl((int) id);
        CategoryDto category = new CategoryDto("updated-category");
        HttpHeaders headers = loginWithHeaders(admin);
        HttpEntity httpEntity = new HttpEntity(category, headers);

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 100);
        ResponseEntity<String> response =  restTemplate.exchange(baseUrl, HttpMethod.PUT, httpEntity, String.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // Delete category
    @Test
    public void deleteCategory() {
        // create 1.000 categories
        long id = categoryUtils.createCategory("the-chosen-one");
        for (int i = 0; i < 999; i++) {
            categoryUtils.createCategory("test-category-" + i);
        }

        String baseUrl = categoryUrl((int) id);
        HttpHeaders headers = loginWithHeaders(admin);
        HttpEntity httpEntity = new HttpEntity(headers);

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 100);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.DELETE, httpEntity, String.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
