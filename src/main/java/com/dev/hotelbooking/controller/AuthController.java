package com.dev.hotelbooking.controller;

import com.dev.hotelbooking.dto.request.LoginRequest;
import com.dev.hotelbooking.model.User;
import com.dev.hotelbooking.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IUserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Account created. We’ve sent a verification link to your email.\n" +
                "Please click the link to activate your account.");
    }

    @GetMapping("/verify/account")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        userService.verifyAccount(token);
        return ResponseEntity.ok("Account verified successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.login(loginRequest));
    }

    @GetMapping("/verify/{email}")
    public ResponseEntity<Map<String, Object>> getVerifyStatus(@PathVariable String email) {
        boolean verifyStatus = userService.checkVerifyStatus(email);
        return ResponseEntity.ok(Map.of("isVerified", verifyStatus));
    }
}
