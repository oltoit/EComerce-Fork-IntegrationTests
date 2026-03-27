package com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.tests;

import com.github.damiox.ecommerce.api.controller.objects.CredentialsDto;
import com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.PerfTimer;
import com.github.damiox.ecommerce.api.controller.performance.timeBehaviour.TimeBehaviourIntegrationTestBase;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthenticationPerfTimeTest extends TimeBehaviourIntegrationTestBase {
    @Test
    public void login() {
        CredentialsDto credentals = new CredentialsDto(user1.name, user1.password);
        HttpEntity httpEntity = new HttpEntity<>(credentals, null);
        String url = loginUrl();

        PerfTimer.start();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        PerfTimer.stop();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
