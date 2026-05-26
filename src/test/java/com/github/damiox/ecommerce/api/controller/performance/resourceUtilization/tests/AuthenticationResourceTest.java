package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.tests;

import com.github.damiox.ecommerce.api.controller.objects.CredentialsDto;
import com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceUtilizationIntegrationTestBase;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthenticationResourceTest extends ResourceUtilizationIntegrationTestBase {
    @Test
    public void login() {
        CredentialsDto credentials = new CredentialsDto(user1.name, user1.password);
        HttpEntity httpEntity = new HttpEntity<>(credentials, null);
        String url = loginUrl();

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
