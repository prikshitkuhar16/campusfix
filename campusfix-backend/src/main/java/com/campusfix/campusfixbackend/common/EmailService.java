package com.campusfix.campusfixbackend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final String fromEmail;

    public EmailService(JavaMailSender javaMailSender, @Value("${spring.mail.username}") String fromEmail) {
        this.javaMailSender = javaMailSender;
        this.fromEmail = fromEmail;
    }

    /**
     * Send an invite email to the specified address.
     */
    public void sendInviteEmail(String toEmail, String inviteToken, String role) {
        String inviteLink = "https://campusfix.app/invite?token=" + inviteToken;
        String subject = "CampusFix - Profile Invitation";
        String message = "You have been invited to join CampusFix as a " + role + ".\n\n" +
                "Click the link below to accept the invitation:\n" + inviteLink + "\n\n" +
                "This link will expire in 48 hours.";

        sendEmail(toEmail, subject, message);
    }

    public void sendCampusCreationOtp(String toEmail, String otp) {
        String subject = "CampusFix - Verify Campus Creation";
        String message = "Your OTP for creating a new campus is: " + otp + "\n\n" +
                "This code will expire in 10 minutes.";

        sendEmail(toEmail, subject, message);
    }

    public void sendStudentLoginOtp(String toEmail, String otp) {
        String subject = "CampusFix - Student Login Verification";
        String message = "Your OTP for student login is: " + otp + "\n\n" +
                "If you did not request this code, please ignore this email.\n" +
                "This code will expire in 10 minutes.";

        sendEmail(toEmail, subject, message);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
            log.info("Email sent to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to={}", to, e);
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }
}
