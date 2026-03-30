package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.tests;

import com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceSampler;
import com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceUtilizationIntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.utils.CategoryUtils;
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
public class CategorySubcategoriesResourceTest extends ResourceUtilizationIntegrationTestBase {

    @Autowired
    private CategoryUtils categoryUtils;

    @Test
    public void getSubcategories() {
        // create 1.000 products
        long parentId = categoryUtils.createCategory("parent");
        for (int i = 0; i < 1_000; i++) {
            categoryUtils.createSubcategory("test-" + i, parentId);
        }

        String subcategoriesUrl = subcategoriesUrl(parentId);
        HttpEntity httpEntity = new HttpEntity<>(loginWithHeaders(user1));

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 600);
        ResponseEntity<String> response = restTemplate.exchange(subcategoriesUrl, HttpMethod.GET, httpEntity, String.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void addSubcategory() {
        // create 1.000 products
        long parentId = categoryUtils.createCategory("parent");
        for (int i = 0; i < 1_000; i++) {
            categoryUtils.createSubcategory("test-" + i, parentId);
        }
        long childId = categoryUtils.createCategory("child");

        String subcategoryUrl = subcategoryUrl(parentId, childId);
        HttpEntity httpEntity = new HttpEntity(loginWithHeaders(admin));

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 2400);
        ResponseEntity<String> response = restTemplate.exchange(subcategoryUrl, HttpMethod.POST, httpEntity, String.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    public void removeSubcategory() {
        // create 1.000 products
        long parentId = categoryUtils.createCategory("parent");
        for (int i = 0; i < 1_000; i++) {
            categoryUtils.createSubcategory("test-" + i, parentId);
        }
        long childId = categoryUtils.createSubcategory("child", parentId);

        String subcategoryUrl = subcategoryUrl(parentId, childId);
        HttpEntity httpEntity = new HttpEntity(loginWithHeaders(admin));

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 2400);
        ResponseEntity<String> response = restTemplate.exchange(subcategoryUrl, HttpMethod.DELETE, httpEntity, String.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
