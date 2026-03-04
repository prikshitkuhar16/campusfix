package com.campusfix.campusfixbackend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    /**
     * Send an invite email to the specified address.
     * Currently logs the invite; integrate with an SMTP provider for production.
     */
    public void sendInviteEmail(String toEmail, String inviteToken, String role) {
        String inviteLink = "https://campusfix.app/invite?token=" + inviteToken;
        log.info("Sending invite email to={}, role={}, link={}", toEmail, role, inviteLink);
        // TODO: Integrate with email provider (SendGrid, AWS SES, etc.)
    }
}
