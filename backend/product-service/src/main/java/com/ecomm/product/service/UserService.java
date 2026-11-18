package com.ecomm.product.service;

import com.ecomm.product.dto.user.*;
import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    UserDto getUserById(String id);
    UserDto createUser(CreateUserRequest request);
    UserDto updateUser(String id, UpdateUserRequest request);
    void deleteUser(String id);
    AuthLoginResponse authenticate(AuthLoginRequest request);
    UserDto register(CreateUserRequest request);
}
