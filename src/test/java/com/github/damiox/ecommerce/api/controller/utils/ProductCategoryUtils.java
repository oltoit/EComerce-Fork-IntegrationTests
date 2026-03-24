package com.github.damiox.ecommerce.api.controller.utils;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductCategoryUtils extends AbstractDBAccess {

    public void addProductToCategory(long productId, long categoryId) {
        jdbcTemplate.update(
                "INSERT INTO app_product_category(productid, categoryid) VALUES(?, ?)",
                productId, categoryId
        );
    }

    public List<Long> getCategoryIds(long productId) {
        String query = String.format("SELECT * FROM app_product_category WHERE productid = %s", productId);
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
        return results.stream().map(e -> (Long) e.get("categoryid")).collect(Collectors.toList());
    }
}