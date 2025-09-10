package com.dev.hotelbooking.controller;

import com.dev.hotelbooking.dto.request.UserRequest;
import com.dev.hotelbooking.dto.response.UserResponse;
import com.dev.hotelbooking.model.User;
import com.dev.hotelbooking.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.dev.hotelbooking.constants.Constants.PHOTO_DIRECTORY;
import static org.springframework.util.MimeTypeUtils.IMAGE_JPEG_VALUE;
import static org.springframework.util.MimeTypeUtils.IMAGE_PNG_VALUE;

@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.FOUND).body(userService.getUsers());
    }

    @GetMapping("/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUser(email));
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #email == authentication.name)")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        userService.deleteUser(email);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("/{email}")
    @PreAuthorize("(hasRole('ADMIN') or hasRole('USER')) and #email == authentication.name")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String email,
                                                   @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(email, userRequest));
    }

    @PatchMapping("/{id}/avatar")
    @PreAuthorize("(hasRole('USER') or hasRole('ADMIN')) and #id == @userService.getUser(authentication.name).id")
    public ResponseEntity<String> uploadAvatar(@PathVariable Long id,
                                               @RequestParam("photo") MultipartFile photo) {
        String avatarUrl = userService.uploadAvatar(id, photo);
        return ResponseEntity.ok(avatarUrl);
    }

    @GetMapping(path = "/photo/{filename}", produces = {IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE})
    public byte[] getImage(@PathVariable String filename) throws IOException {
        return Files.readAllBytes(Paths.get(PHOTO_DIRECTORY + filename));
    }
}
