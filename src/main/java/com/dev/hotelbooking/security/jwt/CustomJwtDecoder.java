package com.dev.hotelbooking.security.jwt;

import com.dev.hotelbooking.exception.AppException;
import com.dev.hotelbooking.enums.ErrorCode;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    private final JwtService jwtService;
    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Value("${auth.token.jwtSecret}")
    private String jwtSecret;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            jwtService.verifyToken(token);
        } catch (JOSEException | ParseException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKey secretKey = new SecretKeySpec(this.jwtSecret.getBytes(), "SHA256");
            nimbusJwtDecoder = NimbusJwtDecoder
                    .withSecretKey(secretKey)
                    .macAlgorithm(MacAlgorithm.HS256)
                    .build();
        }

        return nimbusJwtDecoder.decode(token);
    }
}
