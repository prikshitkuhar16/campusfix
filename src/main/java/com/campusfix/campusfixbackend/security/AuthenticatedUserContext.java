package com.campusfix.campusfixbackend.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserContext {

    public AuthenticatedUser getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }

        AuthenticatedUser user = new AuthenticatedUser();
        user.setFirebaseUid(jwt.getSubject());
        user.setEmail(jwt.getClaimAsString("email"));
        user.setEmailVerified(jwt.getClaimAsBoolean("email_verified"));

        return user;
    }
}
