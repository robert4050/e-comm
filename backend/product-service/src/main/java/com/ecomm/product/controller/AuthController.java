package com.ecomm.product.controller;

import com.ecomm.product.dto.user.AuthLoginRequest;
import com.ecomm.product.dto.user.AuthLoginResponse;
import com.ecomm.product.dto.user.CreateUserRequest;
import com.ecomm.product.dto.user.UserDto;
import com.ecomm.product.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        AuthLoginResponse response = userService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody CreateUserRequest request) {
        log.info("Received self-registration request for email: {}", request.getEmail());
        UserDto registered = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(registered);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.getUserById(authentication.getName()));
    }
}
