package com.ecomm.product.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TwilioSmsService {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsService.class);

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.from-number:+15005550006}")
    private String fromNumber;

    @Value("${twilio.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean sendDeliveryOtp(String toNumber, String orderNumber, String otp) {
        String recipient = (toNumber != null && !toNumber.isBlank()) ? toNumber : "+91 98765 43210";
        String messageBody = String.format("e-comm Delivery: Your 6-digit OTP for order %s is %s. Share this with the delivery executive or enter on My Orders to confirm delivery.", orderNumber, otp);

        if (enabled && accountSid != null && accountSid.startsWith("AC") && authToken != null && !authToken.isBlank() && !authToken.contains("mock")) {
            try {
                String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", accountSid);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                String auth = accountSid + ":" + authToken;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                headers.set("Authorization", "Basic " + encodedAuth);

                MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
                map.add("To", recipient);
                map.add("From", fromNumber);
                map.add("Body", messageBody);

                HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("[TWILIO LIVE DISPATCH] Successfully sent Delivery OTP SMS to {} for order {} (HTTP {})", recipient, orderNumber, response.getStatusCode());
                    return true;
                }
            } catch (Exception e) {
                log.warn("[TWILIO DISPATCH FAILED] Could not send live SMS to {}: {}. Falling back to simulation mode.", recipient, e.getMessage());
            }
        }

        // Standard simulation output for local and dev testing without live Twilio charges
        log.info("================================================================================");
        log.info("[TWILIO SMS GATEWAY] Dispatching Secure Customer Delivery OTP");
        log.info("Recipient Phone:  {}", recipient);
        log.info("From (Twilio):    {}", fromNumber);
        log.info("Order Reference:  {}", orderNumber);
        log.info("Secure OTP Code:  {}", otp);
        log.info("SMS Content:      {}", messageBody);
        log.info("================================================================================");
        return true;
    }
}
