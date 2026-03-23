package com.github.damiox.ecommerce.api.controller.functionality;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.objects.CategoryDto;
import com.github.damiox.ecommerce.api.controller.utils.CategoryUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CategoryControllerFunctionalityTest extends IntegrationTestBase {

    private String baseUrl;

    @Autowired
    private CategoryUtils categoryUtils;

    @Before
    public void init() {
        baseUrl = categoriesUrl();
    }

    @Test
    public void getAllCategories() {
        HttpHeaders headers = loginWithHeaders(user1);
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getAllCategoriesNotLoggedIn() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void getCategory() {
        long id = categoryUtils.createCategory("test-category");

        ResponseEntity<String> response = restTemplate.exchange(categoryUrl((int) id), HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getCategoryNotFound() {
        ResponseEntity<String> response = restTemplate.exchange(categoryUrl(1), HttpMethod.GET, new HttpEntity<>(loginWithHeaders(user1)), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void getCategoryNotLoggedIn() {
        long id = categoryUtils.createCategory("test-category");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(categoryUrl((int) id), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void createCategory() {
        ResponseEntity<Map> response = createCategory(loginWithHeaders(admin), "new-category");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        long id = categoryUtils.getId(response);
        Map<String, Object> fromDb = categoryUtils.getCategoryAsMap(id);
        assertThat(fromDb.get("name")).isEqualTo("new-category");
    }

    @Test
    public void createCategoryNotLoggedIn() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = createCategory(headers, "new-category");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void createCategoryAsNormalUser() {
        ResponseEntity<Map> response = createCategory(loginWithHeaders(user1), "new-category");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void createCategoryEmptyName() {
        ResponseEntity<Map> response = createCategory(loginWithHeaders(admin), "");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void updateCategory() {
        long id = categoryUtils.createCategory("original-category");

        ResponseEntity<Map> response = updateCategory(loginWithHeaders(admin), id, "updated-category");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Map<String, Object> fromDb = categoryUtils.getCategoryAsMap(id);
        assertThat(fromDb.get("name")).isEqualTo("updated-category");
    }

    @Test
    public void updateCategoryNotLoggedIn() {
        long id = categoryUtils.createCategory("original-category");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = updateCategory(headers, id, "updated-category");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        Map<String, Object> fromDb = categoryUtils.getCategoryAsMap(id);
        assertThat(fromDb.get("name")).isEqualTo("original-category");
    }

    @Test
    public void updateCategoryAsNormalUser() {
        long id = categoryUtils.createCategory("original-category");

        ResponseEntity<Map> response = updateCategory(loginWithHeaders(user1), id, "updated-category");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        Map<String, Object> fromDb = categoryUtils.getCategoryAsMap(id);
        assertThat(fromDb.get("name")).isEqualTo("original-category");
    }

    @Test
    public void updateCategoryNotFound() {
        ResponseEntity<Map> response = updateCategory(loginWithHeaders(admin), 1, "updated-category");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void updateCategoryEmptyName() {
        long id = categoryUtils.createCategory("original-category");

        ResponseEntity<Map> response = updateCategory(loginWithHeaders(admin), id, "");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Map<String, Object> fromDb = categoryUtils.getCategoryAsMap(id);
        assertThat(fromDb.get("name")).isEqualTo("original-category");
    }

    @Test
    public void deleteCategory() {
        long id = categoryUtils.createCategory("test-category");

        ResponseEntity<Map> response = deleteCategory(loginWithHeaders(admin), id);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThatThrownBy(() -> categoryUtils.getCategoryAsMap(id))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    public void deleteCategoryNotLoggedIn() {
        long id = categoryUtils.createCategory("test-category");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = deleteCategory(headers, id);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        Map<String, Object> fromDb = categoryUtils.getCategoryAsMap(id);
        assertThat(fromDb.get("name")).isEqualTo("test-category");
    }

    @Test
    public void deleteCategoryAsNormalUser() {
        long id = categoryUtils.createCategory("test-category");

        ResponseEntity<Map> response = deleteCategory(loginWithHeaders(user1), id);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        Map<String, Object> fromDb = categoryUtils.getCategoryAsMap(id);
        assertThat(fromDb.get("name")).isEqualTo("test-category");
    }

    @Test
    public void deleteCategoryNonExistent() {
        ResponseEntity<Map> response = deleteCategory(loginWithHeaders(admin), 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    // Private helper functions

    private ResponseEntity<Map> createCategory(HttpHeaders headers, String name) {
        return restTemplate.exchange(baseUrl, HttpMethod.POST, new HttpEntity<>(new CategoryDto(name), headers), Map.class);
    }

    private ResponseEntity<Map> updateCategory(HttpHeaders headers, long id, String name) {
        return restTemplate.exchange(categoryUrl((int) id), HttpMethod.PUT, new HttpEntity<>(new CategoryDto(name), headers), Map.class);
    }

    private ResponseEntity<Map> deleteCategory(HttpHeaders headers, long id) {
        return restTemplate.exchange(categoryUrl((int) id), HttpMethod.DELETE, new HttpEntity<>(headers), Map.class);
    }
}