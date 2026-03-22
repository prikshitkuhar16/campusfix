package com.campusfix.campusfixbackend.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticatedUser {
    private String firebaseUid;
    private String email;
    private Boolean emailVerified;
}
