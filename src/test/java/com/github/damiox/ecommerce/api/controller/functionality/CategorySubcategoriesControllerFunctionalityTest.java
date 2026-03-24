package com.github.damiox.ecommerce.api.controller.functionality;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.utils.CategoryUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CategorySubcategoriesControllerFunctionalityTest extends IntegrationTestBase {

    @Autowired
    private CategoryUtils categoryUtils;

    @Test
    public void getSubcategories() {
        long parentId = categoryUtils.createCategory("parent");

        ResponseEntity<String> response = restTemplate.exchange(subcategoriesUrl(parentId), HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getSubcategoriesNotLoggedIn() {
        long parentId = categoryUtils.createCategory("parent");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange(subcategoriesUrl(parentId), HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void getSubcategoriesParentNotFound() {
        ResponseEntity<String> response = restTemplate.exchange(subcategoriesUrl(1), HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void addSubcategory() {
        long parentId = categoryUtils.createCategory("parent");
        long childId = categoryUtils.createCategory("child");

        ResponseEntity<Map> response = addSubcategory(loginWithHeaders(admin), parentId, childId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // get child category
        Map<String, Object> childCategory = categoryUtils.getCategoryAsMap(childId);
        assertThat(childCategory.get("parentid")).isEqualTo(parentId);
    }

    @Test
    public void addSubcategoryNotLoggedIn() {
        long parentId = categoryUtils.createCategory("parent");
        long childId = categoryUtils.createCategory("child");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = addSubcategory(headers, parentId, childId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // verify in db that parentId is still null
        Map<String, Object> childFromDb = categoryUtils.getCategoryAsMap(childId);
        assertThat(childFromDb.get("parentid")).isNull();
    }

    @Test
    public void addSubcategoryAsNormalUser() {
        long parentId = categoryUtils.createCategory("parent");
        long childId = categoryUtils.createCategory("child");

        ResponseEntity<Map> response = addSubcategory(loginWithHeaders(user1), parentId, childId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // verify in db that parentId is still null
        Map<String, Object> childFromDb = categoryUtils.getCategoryAsMap(childId);
        assertThat(childFromDb.get("parentid")).isNull();
    }

    @Test
    public void addSubcategoryParentNotFound() {
        long childId = categoryUtils.createCategory("child");

        ResponseEntity<Map> response = addSubcategory(loginWithHeaders(admin), 1, childId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        Map<String, Object> childFromDb = categoryUtils.getCategoryAsMap(childId);
        assertThat(childFromDb.get("parentid")).isNull();
    }

    @Test
    public void addSubcategoryChildNotFound() {
        long parentId = categoryUtils.createCategory("parent");

        ResponseEntity<Map> response = addSubcategory(loginWithHeaders(admin), parentId, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void addSubcategoryAlreadyAssociated() {
        long parentId = categoryUtils.createCategory("parent");
        long childId = categoryUtils.createSubcategory("child", parentId);

        ResponseEntity<Map> response = addSubcategory(loginWithHeaders(admin), parentId, childId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void updateSubCategory() {
        long parentId = categoryUtils.createCategory("parent");
        long parent2Id = categoryUtils.createCategory("parent_2");
        long childId = categoryUtils.createSubcategory("child", parent2Id);

        ResponseEntity<Map> response = addSubcategory(loginWithHeaders(admin), parentId, childId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // verify that parentId was changed
        Map<String, Object> childFromDb = categoryUtils.getCategoryAsMap(childId);
        assertThat(childFromDb.get("parentid")).isEqualTo(parentId);
    }

    @Test
    public void removeSubcategory() {
        long parentId = categoryUtils.createCategory("parent");
        long childId = categoryUtils.createSubcategory("child", parentId);

        ResponseEntity<Map> response = removeSubcategory(loginWithHeaders(admin), parentId, childId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // verify in db that parentid is now null
        Map<String, Object> childFromDb = categoryUtils.getCategoryAsMap(childId);
        assertThat(childFromDb.get("parentid")).isNull();
    }

    @Test
    public void removeSubcategoryNotLoggedIn() {
        long parentId = categoryUtils.createCategory("parent");
        long childId = categoryUtils.createSubcategory("child", parentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = removeSubcategory(headers, parentId, childId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // verify in db that parentid is still set
        Map<String, Object> childFromDb = categoryUtils.getCategoryAsMap(childId);
        assertThat(childFromDb.get("parentid")).isEqualTo(parentId);
    }

    @Test
    public void removeSubcategoryAsNormalUser() {
        long parentId = categoryUtils.createCategory("parent");
        long childId = categoryUtils.createSubcategory("child", parentId);

        ResponseEntity<Map> response = removeSubcategory(loginWithHeaders(user1), parentId, childId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // verify in db that parentid is still set
        Map<String, Object> childFromDb = categoryUtils.getCategoryAsMap(childId);
        assertThat(childFromDb.get("parentid")).isEqualTo(parentId);
    }

    @Test
    public void removeSubcategoryParentNotFound() {
        long childId = categoryUtils.createCategory("child");

        ResponseEntity<Map> response = removeSubcategory(loginWithHeaders(admin), 1, childId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void removeSubcategoryChildNotFound() {
        long parentId = categoryUtils.createCategory("parent");

        ResponseEntity<Map> response = removeSubcategory(loginWithHeaders(admin), parentId, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void removeSubcategoryNotAssociated() {
        long parentId = categoryUtils.createCategory("parent");
        long childId = categoryUtils.createCategory("child");

        ResponseEntity<Map> response = removeSubcategory(loginWithHeaders(admin), parentId, childId);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }


    // Private helper functions

    private ResponseEntity<Map> addSubcategory(HttpHeaders headers, long parentId, long childId) {
        return restTemplate.exchange(
                subcategoryUrl(parentId, childId),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                Map.class
        );
    }

    private ResponseEntity<Map> removeSubcategory(HttpHeaders headers, long parentId, long childId) {
        return restTemplate.exchange(
                subcategoryUrl(parentId, childId),
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Map.class
        );
    }
}