package com.github.damiox.ecommerce.api.controller.functionality;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.objects.CategoryDto;
import com.github.damiox.ecommerce.api.controller.objects.ProductDto;
import com.github.damiox.ecommerce.api.controller.utils.CategoryUtils;
import com.github.damiox.ecommerce.api.controller.utils.ProductCategoryUtils;
import com.github.damiox.ecommerce.api.controller.utils.ProductUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/*
Tests to test whether every endpoint sends a correct hateoas response.
Only Endpoints are tested that actually return a body.
*/

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HateoasFunctionalityTest extends IntegrationTestBase {

    private String productsBaseUrl;
    private String categoriesBaseUrl;

    @Autowired
    private ProductUtils productUtils;
    @Autowired
    private CategoryUtils categoryUtils;
    @Autowired
    private ProductCategoryUtils productCategoryUtils;

    @Value("classpath:HATEOAS/getProducts.json")
    private Resource getProductsResponse;

    @Value("classpath:HATEOAS/getProduct.json")
    private Resource getProductResponse;

    @Value("classpath:HATEOAS/createProduct.json")
    private Resource createProductResponse;

    @Value("classpath:HATEOAS/updateProduct.json")
    private Resource updateProductResponse;

    @Value("classpath:HATEOAS/getCategories.json")
    private Resource getCategoriesResponse;

    @Value("classpath:HATEOAS/getCategory.json")
    private Resource getCategoryResponse;

    @Value("classpath:HATEOAS/createCategory.json")
    private Resource createCategoryResponse;

    @Value("classpath:HATEOAS/updatedCategory.json")
    private Resource updateCategoryResponse;

    @Value("classpath:HATEOAS/getSubcategories.json")
    private Resource getSubcategoriesResponse;

    @Value("classpath:HATEOAS/addSubcategory.json")
    private Resource addSubcategoryResponse;

    @Value("classpath:HATEOAS/getCategoryProducts.json")
    private Resource getCategoryProductsResponse;

    @Value("classpath:HATEOAS/addCategoryProduct.json")
    private Resource addCategoryProductResponse;

    @Before
    public void init() {
        productsBaseUrl = productsUrl();
        categoriesBaseUrl = categoriesUrl();
    }

    // ProductController Tests
    @Test
    public void getProducts() throws IOException {
        productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<String> response = restTemplate.exchange(
                productsBaseUrl, HttpMethod.GET,
                new HttpEntity<>(loginWithHeaders(user1)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(getProductsResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);
    }

    @Test
    public void getProduct() throws IOException {
        long id = productUtils.createProduct(defaultProduct, user1.id);
        String url = productUrl((int) id);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET,
                new HttpEntity<>(loginWithHeaders(user1)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(getProductResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);
    }

    @Test
    public void createProduct() throws IOException {
        ResponseEntity<String> response = restTemplate.exchange(
                productsBaseUrl, HttpMethod.POST,
                new HttpEntity<>(defaultProduct, loginWithHeaders(user1)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JSONAssert.assertEquals(createProductResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);

        // Test that href is valid
        JsonNode root = new ObjectMapper().readTree(response.getBody());
        String selfHref = root.path("_links").path("self").path("href").asText();
        ResponseEntity<Map> getResponse = restTemplate.exchange(
                selfHref, HttpMethod.GET,
                new HttpEntity<>(loginWithHeaders(user1)), Map.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void updateProduct() throws IOException {
        long id = productUtils.createProduct(defaultProduct, user1.id);

        ResponseEntity<String> response = restTemplate.exchange(
                productsBaseUrl + "/" + id, HttpMethod.PUT,
                new HttpEntity<>(new ProductDto("updated", "EUR", 12.00), loginWithHeaders(user1)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(updateProductResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);

        // Test that href is valid
        JsonNode root = new ObjectMapper().readTree(response.getBody());
        String selfHref = root.path("_links").path("self").path("href").asText();
        ResponseEntity<Map> getResponse = restTemplate.exchange(
                selfHref, HttpMethod.GET,
                new HttpEntity<>(loginWithHeaders(user1)), Map.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    // CategoryController Tests
    @Test
    public void getCategories() throws IOException {
        categoryUtils.createCategory("test-category");

        // Using List here turns _link into link for some reason
        ResponseEntity<String> response = restTemplate.exchange(
                categoriesBaseUrl, HttpMethod.GET,
                new HttpEntity<>(loginWithHeaders(user1)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(getCategoriesResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);
    }

    @Test
    public void getCategory() throws IOException {
        long id = categoryUtils.createCategory("test-category");
        String url = categoryUrl((int) id);

        ResponseEntity<String> response = restTemplate.exchange(
                url , HttpMethod.GET,
                new HttpEntity<>(loginWithHeaders(user1)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(getCategoryResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);
    }

    @Test
    public void createCategory() throws IOException {
        ResponseEntity<String> response = restTemplate.exchange(
                categoriesBaseUrl, HttpMethod.POST,
                new HttpEntity<>(new CategoryDto("new-category"), loginWithHeaders(admin)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JSONAssert.assertEquals(createCategoryResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);

        // Test that href is valid
        JsonNode root = new ObjectMapper().readTree(response.getBody());
        String selfHref = root.path("_links").path("self").path("href").asText();
        ResponseEntity<Map> getResponse = restTemplate.exchange(
                selfHref, HttpMethod.GET,
                new HttpEntity<>(loginWithHeaders(user1)), Map.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void updateCategory() throws IOException {
        long id = categoryUtils.createCategory("original-category");
        String url = categoryUrl((int) id);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.PUT,
                new HttpEntity<>(new CategoryDto("updated-category"), loginWithHeaders(admin)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(updateCategoryResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);

        // Test that href is valid
        JsonNode root = new ObjectMapper().readTree(response.getBody());
        String selfHref = root.path("_links").path("self").path("href").asText();
        ResponseEntity<Map> getResponse = restTemplate.exchange(
                selfHref, HttpMethod.GET,
                new HttpEntity<>(loginWithHeaders(user1)), Map.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    // CategorySubcategoriesController Tests
    @Test
    public void getSubcategories() throws IOException {
        long parentId = categoryUtils.createCategory("parent");
        categoryUtils.createSubcategory("child", parentId);

        ResponseEntity<String> response = restTemplate.exchange(
                subcategoriesUrl(parentId), HttpMethod.GET,
                new HttpEntity<>(loginWithHeaders(user1)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(getSubcategoriesResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);

        // check that href is valid
        JsonNode root = new ObjectMapper().readTree(response.getBody()).get(0);
        String selfHref = null;

        for (JsonNode link: root.path("links")) {
            if (link.path("rel").asText().equals("self")) {
                selfHref = link.path("href").asText();
                break;
            }
        }

        ResponseEntity<Map> getResponse = restTemplate.exchange(
                selfHref, HttpMethod.GET,
                new HttpEntity<>(loginWithHeaders(user1)), Map.class
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void addSubcategory() throws IOException {
        long parentId = categoryUtils.createCategory("parent");
        long childId = categoryUtils.createCategory("child");

        ResponseEntity<String> response = restTemplate.exchange(
                subcategoryUrl(parentId, childId), HttpMethod.POST,
                new HttpEntity<>(loginWithHeaders(admin)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JSONAssert.assertEquals(addSubcategoryResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);

        // test that both hrefs are valid
        JsonNode root = new ObjectMapper().readTree(response.getBody());
        String selfLink = root.path("_links").path("self").path("href").asText();
        String subcategoryLink = root.path("_links").path("subcategories").path("href").asText();

        assertThat(selfLink).isEqualTo(categoryUrl((int) parentId));
        assertThat(subcategoryLink).isEqualTo(subcategoriesUrl(parentId));
    }


    // CategoryProductsController Tests
    @Test
    public void getCategoryProducts() throws IOException {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        long categoryId = categoryUtils.createCategory("test-category");
        productCategoryUtils.addProductToCategory(productId, categoryId);

        ResponseEntity<String> response = restTemplate.exchange(
                categoryProductsUrl(categoryId), HttpMethod.GET,
                new HttpEntity<>(loginWithHeaders(user1)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JSONAssert.assertEquals(getCategoryProductsResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);

        // Test that self href is valid
        JsonNode root = new ObjectMapper().readTree(response.getBody());
        String selfHref = root.path("_embedded").path("productResourceList").path(0).path("_links").path("self").path("href").asText();
        assertThat(selfHref).isEqualTo(productUrl((int) productId));
    }

    @Test
    public void addCategoryProduct() throws IOException {
        long productId = productUtils.createProduct(defaultProduct, user1.id);
        long categoryId = categoryUtils.createCategory("test-category");
        String url = productUrl((int) productId);

        ResponseEntity<String> response = restTemplate.exchange(
                categoryProductUrl(categoryId, productId), HttpMethod.POST,
                new HttpEntity<>(loginWithHeaders(user1)), String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JSONAssert.assertEquals(addCategoryProductResponse.getContentAsString(Charset.defaultCharset()), response.getBody(), false);

        // Test that self href is valid
        JsonNode root = new ObjectMapper().readTree(response.getBody());
        String selfHref = root.path("_links").path("self").path("href").asText();
        assertThat(selfHref).isEqualTo(productUrl((int) productId));
    }
}
