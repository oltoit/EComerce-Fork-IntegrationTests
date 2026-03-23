package com.github.damiox.ecommerce.api.controller.objects;

public class User {
    public String name;
    public String password;
    public Role role;
    public long id;

    public User() {}

    public User(String name, String password, Role role, long id) {
        this.name = name;
        this.password = password;
        this.role = role;
        this.id = id;
    }
}
