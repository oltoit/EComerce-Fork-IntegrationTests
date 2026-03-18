package com.github.damiox.ecommerce.service;

import java.sql.SQLException;

public interface DatabaseService {
    void resetDatabase() throws SQLException;
}
