package com.dev.hotelbooking.security.jwt;

import com.dev.hotelbooking.enums.ErrorCode;
import com.dev.hotelbooking.exception.AppException;
import com.dev.hotelbooking.exception.TokenAuthenticationException;
import com.dev.hotelbooking.model.User;
import com.dev.hotelbooking.security.user.HotelUserDetails;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtService {
    @Value("${auth.token.jwtSecret}")
    private String jwtSecret;

    @Value("${auth.token.expirationInMils}")
    private long expirationInMils;

    public String generateJwtToken(User user) throws JOSEException {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issueTime(new Date())
                .expirationTime(expirationInMils > 0 ? new Date(System.currentTimeMillis() + expirationInMils) : null)
                .jwtID(UUID.randomUUID().toString())
                .claim("roles", buildScopes(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        jwsObject.sign(new MACSigner(jwtSecret.getBytes()));

        return jwsObject.serialize();
    }

    public SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);
        Date expiredTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        boolean isVerified = signedJWT.verify(verifier);

        if (!(isVerified && expiredTime.after(new Date()))) {
            throw new TokenAuthenticationException("Token expired ");
        }

        return signedJWT;
    }

    public List<String> buildScopes(User user) {
        List<String> scopes = new ArrayList<>();
        user.getRoles().forEach(role -> scopes.add("ROLE_" + role.getName()));
//        return authentication.getAuthorities()
//                .stream()
//                .map(GrantedAuthority::getAuthority)
//                .toList();
        return scopes;
    }
}
