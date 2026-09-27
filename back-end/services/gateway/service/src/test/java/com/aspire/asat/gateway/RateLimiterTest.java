package com.aspire.asat.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RateLimiterTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final String publicUrl = "/auth/api/v1/auth/health";

/*    @Test
    void testRateLimiter() {
        // Send 25 requests quickly to exceed the configured burst capacity (20)
        for (int i = 1; i <= 50; i++) {
            int finalI = i;
            webTestClient.get()
                    .uri(publicUrl)
                    .exchange()
                    .expectStatus()
                    .value(status -> {
                        if (finalI <= 20) {
                            // First 20 should pass
                            if (status != 200) {
                                throw new AssertionError("Request " + finalI + " failed unexpectedly");
                            }
                        } else {
                            // Requests after burst capacity should hit rate limit (HTTP 429)
                            if (status != 429) {
                                throw new AssertionError("Request " + finalI + " should be rate limited");
                            }
                        }
                    });
        }
    }*/
}
