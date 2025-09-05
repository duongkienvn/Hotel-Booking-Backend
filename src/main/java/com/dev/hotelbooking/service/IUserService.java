package com.dev.hotelbooking.service;

import com.dev.hotelbooking.dto.request.LoginRequest;
import com.dev.hotelbooking.dto.request.UserRequest;
import com.dev.hotelbooking.dto.response.UserResponse;
import com.dev.hotelbooking.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserService {
    void registerUser(User user);
    List<User> getUsers();
    void deleteUser(String email);
    User getUser(String email);
    String login(LoginRequest loginRequest);
    void verifyAccount(String token);
    boolean checkVerifyStatus(String email);
    UserResponse updateUser(String email, UserRequest userRequest);
    String uploadAvatar(Long userId, MultipartFile file);
}
