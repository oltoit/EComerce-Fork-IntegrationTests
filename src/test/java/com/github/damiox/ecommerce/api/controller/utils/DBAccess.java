package com.github.damiox.ecommerce.api.controller.utils;

import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;

@Service
public class DBAccess extends AbstractDBAccess{
    public void resetDb() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, truncateScript);
            ScriptUtils.executeSqlScript(connection, dataScript);
        }
    }
}
