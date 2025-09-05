package com.dev.hotelbooking.service.impl;

import com.dev.hotelbooking.enums.ErrorCode;
import com.dev.hotelbooking.exception.AppException;
import com.dev.hotelbooking.model.Role;
import com.dev.hotelbooking.model.User;
import com.dev.hotelbooking.repository.RoleRepository;
import com.dev.hotelbooking.repository.UserRepository;
import com.dev.hotelbooking.service.IOAuthUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static com.dev.hotelbooking.enums.AuthProvider.FACEBOOK;
import static com.dev.hotelbooking.enums.AuthProvider.GOOGLE;

@Service
@RequiredArgsConstructor
@Transactional
public class OAuthUserService implements IOAuthUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public User getUser(OAuth2User oAuth2User) {
        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String registrationId = authenticationToken.getAuthorizedClientRegistrationId();

        String email = oAuth2User.getAttribute("email");
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        return registerUser(oAuth2User, registrationId);
    }

    private User registerUser(OAuth2User oAuth2User, String registrationId) {
        User user = new User();
        if (registrationId.equalsIgnoreCase("google")) {
            user.setAuthProvider(GOOGLE);
            user.setEmail(oAuth2User.getAttribute("email"));
            user.setFirstName(oAuth2User.getAttribute("given_name"));
            user.setLastName(oAuth2User.getAttribute("family_name"));
            user.setDefaultImageUrl(oAuth2User.getAttribute("picture"));
        } else if (registrationId.equalsIgnoreCase("facebook")) {
            user.setAuthProvider(FACEBOOK);
            user.setEmail(oAuth2User.getAttribute("email"));
            user.setFirstName(oAuth2User.getAttribute("name"));
            user.setLastName(null);
            Map<String, Object> pictureObj = oAuth2User.getAttribute("picture");
            if (pictureObj != null) {
                Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get("data");
                user.setDefaultImageUrl(dataObj.get("url").toString());
            }
        }

        user.setPassword(null);
        user.setEnabled(true);
        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoles(new HashSet<>(Collections.singletonList(role)));

        return userRepository.save(user);
    }
}
