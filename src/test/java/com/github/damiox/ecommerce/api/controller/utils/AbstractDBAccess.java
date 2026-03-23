package com.github.damiox.ecommerce.api.controller.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

public abstract class AbstractDBAccess {

    @Value("classpath:truncate-all.sql")
    protected Resource truncateScript;

    @Value("classpath:data.sql")
    protected Resource dataScript;


    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    public AbstractDBAccess() {}
}
