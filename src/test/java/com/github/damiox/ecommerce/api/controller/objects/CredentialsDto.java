package com.github.damiox.ecommerce.api.controller.objects;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CredentialsDto {
    @NotNull(message = "username is required")
    @Size(message = "username must be equal to or lower than 50", min = 1, max = 50)
    private String username;
    @NotNull(message = "password is required")
    @Size(message = "password must be equal to or lower than 50", min = 1, max = 50)
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CredentialsDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
