package com.github.damiox.ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class DatabaseServiceImpl implements DatabaseService{

    @Value("classpath:data.sql")
    private Resource dbInitScript;

    @Value("classpath:truncate-all.sql")
    private Resource truncateAllScript;

    private final DataSource dataSource;

    public DatabaseServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    @Override
    public void resetDatabase() throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, truncateAllScript);
            ScriptUtils.executeSqlScript(connection, dbInitScript);
        }
    }
}
