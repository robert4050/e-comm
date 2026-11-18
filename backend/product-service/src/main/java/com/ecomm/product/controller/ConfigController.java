package com.ecomm.product.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Public Configuration Endpoint.
 * Exposes non-sensitive client configuration (Public Keys, Client IDs, Service Flags)
 * sourced from the central .env file to the Angular web applications.
 * Server-side secrets (Twilio Auth Token, Razorpay Secret, Google Secret) are strictly NOT exposed.
 */
@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {

    @Value("${clerk.publishable-key:pk_test_placeholder}")
    private String clerkPublishableKey;

    @Value("${razorpay.key-id:rzp_test_placeholder_key_id}")
    private String razorpayKeyId;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Value("${twilio.enabled:false}")
    private boolean twilioEnabled;

    @GetMapping("/public")
    public ResponseEntity<Map<String, Object>> getPublicConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("clerkPublishableKey", clerkPublishableKey);
        config.put("razorpayKeyId", razorpayKeyId);
        config.put("googleClientId", googleClientId);
        config.put("twilioEnabled", twilioEnabled);
        return ResponseEntity.ok(config);
    }
}
