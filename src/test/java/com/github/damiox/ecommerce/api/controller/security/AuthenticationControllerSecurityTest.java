package com.github.damiox.ecommerce.api.controller.security;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationControllerSecurityTest extends IntegrationTestBase {
    private String baseUrl;

    @Before
    public void init() {
        baseUrl = loginUrl();
    }

    @Test
    public void loginOptions() {
        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.OPTIONS, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void login() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void loginLoggedInAsNormalUser() {
        HttpHeaders headers = loginWithHeaders(user1);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void loginLoggedInAsAdmin() {
        HttpHeaders headers = loginWithHeaders(admin);
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}