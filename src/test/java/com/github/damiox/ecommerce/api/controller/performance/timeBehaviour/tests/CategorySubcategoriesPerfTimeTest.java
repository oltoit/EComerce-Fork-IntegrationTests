package com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.tests;

import com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.PerfTimer;
import com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.TimeBehaviourIntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.utils.CategoryUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CategorySubcategoriesPerfTimeTest extends TimeBehaviourIntegrationTestBase {

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

        PerfTimer.start();
        ResponseEntity<String> response = restTemplate.exchange(subcategoriesUrl, HttpMethod.GET, httpEntity, String.class);
        PerfTimer.stop();

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

        PerfTimer.start();
        ResponseEntity<String> response = restTemplate.exchange(subcategoryUrl, HttpMethod.POST, httpEntity, String.class);
        PerfTimer.stop();

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

        PerfTimer.start();
        ResponseEntity<String> response = restTemplate.exchange(subcategoryUrl, HttpMethod.DELETE, httpEntity, String.class);
        PerfTimer.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
