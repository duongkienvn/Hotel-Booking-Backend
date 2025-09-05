package com.dev.hotelbooking.service;

import com.dev.hotelbooking.model.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface IOAuthUserService {
    User getUser(OAuth2User oAuth2User);
}
