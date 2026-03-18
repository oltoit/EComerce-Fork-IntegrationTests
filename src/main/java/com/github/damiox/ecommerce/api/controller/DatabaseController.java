package com.github.damiox.ecommerce.api.controller;

import com.github.damiox.ecommerce.service.DatabaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

@RestController
@RequestMapping(path = "/admin/db")
public class DatabaseController {

    private final DatabaseService databaseService;

    public DatabaseController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @PostMapping(path = "/reset")
    public ResponseEntity<?> resetDatabase() {
        try {
            databaseService.resetDatabase();
            return ResponseEntity.noContent().build();
        } catch (SQLException e) {
            System.err.printf("DB couldn't be reset: %s%n", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
