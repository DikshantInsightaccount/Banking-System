package com.authservice.authservice.service;

import com.authservice.authservice.dto.GenericMessageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static class OtpEntry {
        final String otp;
        final long expiresAtEpochSec;

        OtpEntry(String otp, long expiresAtEpochSec) {
            this.otp = otp;
            this.expiresAtEpochSec = expiresAtEpochSec;
        }
    }

    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    @Value("${otp.length:4}")
    private int otpLength;

    @Value("${otp.expiry-seconds:300}")
    private int expirySeconds;

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public GenericMessageResponse sendOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        String otp = generateOtp(otpLength);
        long expiresAt = Instant.now().getEpochSecond() + expirySeconds;

        store.put(email.toLowerCase().trim(), new OtpEntry(otp, expiresAt));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("harshithareddy035@gmail.com");
        message.setTo(email.trim());
        message.setSubject("Your OTP for Registration");
        message.setText(
                "Your OTP is: " + otp + "\n\n" +
                        "This OTP is valid for " + (expirySeconds / 60) + " minutes.\n" +
                        "Do not share this OTP with anyone."
        );

        try {
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }

        return new GenericMessageResponse("OTP sent to email");
    }

    public boolean verifyOtp(String email, String otp) {
        if (email == null || email.trim().isEmpty()) return false;
        if (otp == null || otp.trim().isEmpty()) return false;

        String key = email.toLowerCase().trim();
        OtpEntry entry = store.get(key);
        if (entry == null) return false;

        long now = Instant.now().getEpochSecond();
        if (now > entry.expiresAtEpochSec) {
            store.remove(key);
            return false;
        }

        boolean ok = entry.otp.equals(otp.trim());
        if (ok) {
            store.remove(key); // this prevents ordinary replay by preventing the same otp to be used second time..
        }
        return ok;
    }

    private String generateOtp(int len) {
        int min = (int) Math.pow(10, len - 1);
        int max = (int) Math.pow(10, len) - 1;
        int val = random.nextInt(max - min + 1) + min;
        return String.valueOf(val);
    }
}
