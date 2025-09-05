package com.dev.hotelbooking.service.impl;

import com.dev.hotelbooking.dto.request.LoginRequest;
import com.dev.hotelbooking.dto.request.UserRequest;
import com.dev.hotelbooking.dto.response.UserResponse;
import com.dev.hotelbooking.enums.ErrorCode;
import com.dev.hotelbooking.event.UserEvent;
import com.dev.hotelbooking.exception.AppException;
import com.dev.hotelbooking.model.*;
import com.dev.hotelbooking.repository.*;
import com.dev.hotelbooking.security.jwt.JwtService;
import com.dev.hotelbooking.service.IUserService;
import com.nimbusds.jose.JOSEException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.dev.hotelbooking.constants.Constants.PHOTO_DIRECTORY;
import static com.dev.hotelbooking.constants.Constants.USER;
import static com.dev.hotelbooking.enums.AuthProvider.LOCAL;
import static com.dev.hotelbooking.enums.ConfirmationType.VERIFY_EMAIL;
import static com.dev.hotelbooking.enums.EventType.REGISTRATION;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserService implements IUserService {
    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    final RoleRepository roleRepository;
    final AuthenticationManager authenticationManager;
    final ApplicationEventPublisher publisher;
    final ConfirmationRepository confirmationRepository;
    final JwtService jwtService;
    final BookingRepository bookingRepository;
    final TestimonialRepository testimonialRepository;

    @Value("${api.prefix}")
    String apiPrefix;

    @Override
    public void registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByName(USER)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoles(new HashSet<>(Collections.singletonList(role)));
        user.setEnabled(false);
        user.setDefaultImageUrl("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQkjc6ZLAjLDnOktQg664DKbqQ_C8vCFL94gQ&s");
        user.setAuthProvider(LOCAL);
        userRepository.save(user);
        Confirmation confirmation = new Confirmation(user, VERIFY_EMAIL);
        confirmationRepository.save(confirmation);
        publisher.publishEvent(new UserEvent(user, REGISTRATION, Map.of("key", confirmation.getKey())));
    }

    @Override
    public void verifyAccount(String token) {
        Optional<Confirmation> optionalConfirmation = confirmationRepository.findByKey(token);
        if (optionalConfirmation.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        Confirmation confirmation = optionalConfirmation.get();
        User user = confirmation.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        confirmationRepository.delete(confirmation);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(String email) {
        User user = getUser(email);
        List<BookedRoom> bookedRooms = bookingRepository.findAllByGuestEmail(email);
        if (!bookedRooms.isEmpty()) {
            bookingRepository.deleteAll(bookedRooms);
        }
        List<Testimonial> testimonials = testimonialRepository.findAllByUser_Email(email);
        if (!testimonials.isEmpty()) {
            testimonialRepository.deleteAll(testimonials);
        }
        userRepository.delete(user);
    }

    @Override
    public User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED, "User not found with email: " + email));
    }

    @Override
    public UserResponse updateUser(String email, UserRequest userRequest) {
        User existingUser = getUser(userRequest.getEmail());
        existingUser.setFirstName(userRequest.getFirstName());
        existingUser.setLastName(userRequest.getLastName());
        if (userRequest.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(userRequest.getPhoneNumber());
        }
        User updatedUser = userRepository.save(existingUser);
        return userToUserResponse(updatedUser);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        String avatar = photoFunction.apply(userId.toString(), file);
        User user = getUserById(userId);
        user.setImageUrl(avatar);
        userRepository.save(user);
        return avatar;
    }

    private final Function<String, String> fileExtension =
            filename -> Optional.of(filename)
                    .filter(name -> name.contains("."))
                    .map(name -> "." + name.substring(filename.lastIndexOf(".") + 1))
                    .orElse(".png");

    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
        String fileName = id + fileExtension.apply(image.getOriginalFilename());
        try {
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(fileName), REPLACE_EXISTING);
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path(apiPrefix + "/users/photo/" + fileName).toUriString();
        } catch (Exception e) {
            throw new AppException(ErrorCode.FAILED_TO_SAVE_IMAGE);
        }
    };

    @Override
    public String login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        User user = getUser(email);

        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return jwtService.generateJwtToken(user);
        } catch (JOSEException | AuthenticationException e) {
            throw new AppException(ErrorCode.BAD_CREDENTIALS);
        }
    }

    @Override
    public boolean checkVerifyStatus(String email) {
        User user = getUser(email);
        return user.isEnabled();
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    private UserResponse userToUserResponse(User user) {
        List<String> roles = user.getRoles().stream().map(Role::getName).toList();

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .imageUrl(user.getImageUrl())
                .roles(roles)
                .build();
    }
}
