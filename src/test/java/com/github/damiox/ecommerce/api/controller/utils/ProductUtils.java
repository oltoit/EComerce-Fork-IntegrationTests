package com.github.damiox.ecommerce.api.controller.utils;

import com.github.damiox.ecommerce.api.controller.objects.ProductDto;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

@Service
public class ProductUtils extends AbstractDBAccess {
    public long getId(ResponseEntity<Map> map) {
        String href = (String) ((Map) ((Map) map.getBody().get("_links")).get("self")).get("href");
        return Long.parseLong(href.substring(href.lastIndexOf("/") + 1));
    }

    // product queries
    public ProductDto getProduct(long id) throws SQLException {
        String query = "SELECT * FROM app_product WHERE id = " + id;
        Map<String, Object> map =  jdbcTemplate.queryForMap(query);
        return new ProductDto((String) map.get("name"), "EUR", (double) map.get("price"));
    }

    public long createProduct(ProductDto product, long userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO app_product(id, name, price, userid) VALUES(nextval('hibernate_sequence'), ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, product.getName());
            ps.setDouble(2, product.getPrice());
            ps.setLong(3, userId);
            return ps;
        }, keyHolder);

        return (long) keyHolder.getKeys().get("id");
    }
}
