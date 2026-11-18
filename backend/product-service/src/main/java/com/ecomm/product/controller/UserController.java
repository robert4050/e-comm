package com.ecomm.product.controller;

import com.ecomm.product.dto.user.CreateUserRequest;
import com.ecomm.product.dto.user.UpdateUserRequest;
import com.ecomm.product.dto.user.UserDto;
import com.ecomm.product.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Fetching all registered users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") String id) {
        log.info("Fetching user by id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("Creating new user with email: {}, role: {}", request.getEmail(), request.getRole());
        UserDto created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable("id") String id,
            @RequestBody UpdateUserRequest request) {
        log.info("Updating user id: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable("id") String id) {
        log.info("Deleting user id: {}", id);
        userService.deleteUser(id);
        Map<String, Object> response = new HashMap<>();
        response.put("deleted", true);
        response.put("id", id);
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }
}
