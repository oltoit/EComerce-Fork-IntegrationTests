package com.github.damiox.ecommerce.api.controller.utils;

import com.github.damiox.ecommerce.api.controller.objects.ProductDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class ProductUtils {

    public static String getLink(ResponseEntity<Map> product) {
        return (String) ((Map) ((Map) product.getBody().get("_links")).get("self")).get("href");
    }

    public static ResponseEntity<Map> createDefaultProduct(HttpHeaders headers, String productUrl, RestTemplate restTemplate) {
        ProductDto product = new ProductDto("default-test-product", "EUR", 10.00);
        return createProduct(headers, product, productUrl, restTemplate);
    }

    public static ResponseEntity<Map> createProduct(HttpHeaders headers, ProductDto product, String productUrl, RestTemplate restTemplate) {
        HttpEntity entity = new HttpEntity(product, headers);
        return restTemplate.exchange(productUrl, HttpMethod.POST, entity, Map.class);
    }
}
