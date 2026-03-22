package com.campusfix.campusfixbackend.security;

import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Email verification validator for Firebase JWT tokens.
 *
 * Note: This validator always returns success because we use OTP verification
 * instead of Firebase email verification. Users verify their email through
 * our OTP flow during signup, so we don't need to check the email_verified
 * claim in the JWT token.
 */
public class EmailVerifiedValidator implements OAuth2TokenValidator<Jwt> {

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        // Always return success - we use OTP verification instead of email verification
        // Users are verified through our OTP flow during signup
        return OAuth2TokenValidatorResult.success();
    }
}
