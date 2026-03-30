package com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.tests;

import com.github.damiox.ecommerce.api.controller.objects.CredentialsDto;
import com.github.damiox.ecommerce.api.controller.performance.resourceUtilization.ResourceSampler;
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
        CredentialsDto credentals = new CredentialsDto(user1.name, user1.password);
        HttpEntity httpEntity = new HttpEntity<>(credentals, null);
        String url = loginUrl();

        ResourceSampler.start(Thread.currentThread().getStackTrace()[1].getMethodName(), 2400);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        ResourceSampler.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
