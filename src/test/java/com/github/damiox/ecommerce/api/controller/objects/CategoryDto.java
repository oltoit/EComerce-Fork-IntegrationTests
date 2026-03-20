package com.github.damiox.ecommerce.api.controller.objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

public class CategoryDto {
    @NotNull(message = "name is required")
    @Size(message = "name must be equal to or lower than 100", min = 1, max = 100)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryDto() {}

    public CategoryDto(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {return true;}
        if(o == null || getClass() != o.getClass()) {return false;}
        CategoryDto that = (CategoryDto) o;
        return Objects.equals(name, that.name);
    }
}