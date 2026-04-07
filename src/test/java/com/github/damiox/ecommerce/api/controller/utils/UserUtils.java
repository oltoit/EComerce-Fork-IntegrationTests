package com.github.damiox.ecommerce.api.controller.utils;

import com.github.damiox.ecommerce.api.controller.objects.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserUtils extends AbstractDBAccess{

    // Method for creating a user
    public void createUser(User user) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = encoder.encode(user.password);

        String query = "INSERT INTO app_user VALUES(nextval('hibernate_sequence'), '%s', '%s', '%s')".formatted(user.name, password, user.role.name());
        jdbcTemplate.execute(query);
    }
}
