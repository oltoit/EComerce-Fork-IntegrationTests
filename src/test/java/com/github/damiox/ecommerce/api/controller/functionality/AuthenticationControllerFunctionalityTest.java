package com.github.damiox.ecommerce.api.controller.functionality;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import com.github.damiox.ecommerce.api.controller.objects.CredentialsDto;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthenticationControllerFunctionalityTest extends IntegrationTestBase {

    private String baseUrl;

    @Before
    public void init() {
        baseUrl = loginUrl();
    }

    @Test
    public void loginNoCredentials() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void loginWrongPassword() {
        CredentialsDto credentials = new CredentialsDto("admin", "armin");
        HttpEntity entity = new HttpEntity(credentials, null);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void loginWrongUsername() {
        CredentialsDto credentials = new CredentialsDto("armin", "admin");
        HttpEntity entity = new HttpEntity(credentials, null);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void loginNormalUser() {
        CredentialsDto credentials = new CredentialsDto("user1", "user1");
        HttpEntity entity = new HttpEntity(credentials, null);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().contains("token") && response.getBody().contains("Bearer")).isTrue();
    }

    @Test
    public void loginAdminUser() {
        CredentialsDto credentials = new CredentialsDto("admin", "admin");
        HttpEntity entity = new HttpEntity(credentials, null);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().contains("token") && response.getBody().contains("Bearer")).isTrue();
    }
}
