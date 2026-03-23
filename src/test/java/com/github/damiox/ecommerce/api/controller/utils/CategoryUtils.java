package com.github.damiox.ecommerce.api.controller.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

@Service
public class CategoryUtils extends AbstractDBAccess {

    public long getId(ResponseEntity<Map> map) {
        String href = (String) ((Map) ((Map) map.getBody().get("_links")).get("self")).get("href");
        return Long.parseLong(href.substring(href.lastIndexOf("/") + 1));
    }

    public Map<String, Object> getCategoryAsMap(long id) {
        return jdbcTemplate.queryForMap("SELECT * FROM app_category WHERE id = " + id);
    }

    public long createCategory(String name) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO app_category(id, name, parentid) VALUES(nextval('hibernate_sequence'), ?, NULL)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            return ps;
        }, keyHolder);

        return (long) keyHolder.getKeys().get("id");
    }

    public long createSubcategory(String name, long parentId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO app_category(id, name, parentid) VALUES(nextval('hibernate_sequence'), ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, name);
            ps.setLong(2, parentId);
            return ps;
        }, keyHolder);

        return (long) keyHolder.getKeys().get("id");
    }
}