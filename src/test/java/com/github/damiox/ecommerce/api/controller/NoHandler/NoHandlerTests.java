package com.github.damiox.ecommerce.api.controller.NoHandler;

import com.github.damiox.ecommerce.api.controller.IntegrationTestBase;
import org.junit.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

/// This Test is only used to verify that Endpoints without associated handlers return a 418
/// if that is not the case all tests that fail because they aren't implemented need to be counted as a normal failure
public class NoHandlerTests extends IntegrationTestBase {
    @Test
    public void handlerFound() {
        ResponseEntity<String> response = restTemplate.exchange(productsUrl(), HttpMethod.OPTIONS, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void noHandlerFound() {
        String wrongUrl = url("/this-resource-doesnt-exist");

        ResponseEntity<String> response = restTemplate.exchange(wrongUrl, HttpMethod.OPTIONS, null, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.I_AM_A_TEAPOT);
    }
}
