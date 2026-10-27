package com.attendify.service;

import com.attendify.dto.AuthDtos;
import com.attendify.entity.OTPVerification;
import com.attendify.entity.enums.OtpPurpose;
import com.attendify.repository.OTPVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom random = new SecureRandom();

    private final OTPVerificationRepository otpRepository;

    @Value("${attendify.otp.expiration-minutes:5}")
    private int expirationMinutes;

    @Value("${attendify.otp.max-resend-count:5}")
    private int maxResendCount;

    @Value("${attendify.otp.dev-mode:true}")
    private boolean devMode;

    @Value("${attendify.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${attendify.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${attendify.twilio.phone-number:}")
    private String twilioPhoneNumber;

    public OtpService(OTPVerificationRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Transactional
    public AuthDtos.SendOtpResponse sendOtp(String phone, OtpPurpose purpose) {
        String cleanPhone = phone.trim();

        // Check recent unverified OTP
        Optional<OTPVerification> existingOpt = otpRepository
                .findTopByPhoneAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(cleanPhone, purpose);

        int resendCount = 0;
        if (existingOpt.isPresent()) {
            OTPVerification existing = existingOpt.get();
            if (existing.getResendCount() >= maxResendCount && !existing.isExpired()) {
                throw new IllegalStateException("Max OTP resend limit reached for this number. Please wait before trying again.");
            }
            resendCount = existing.getResendCount() + 1;
        }

        // Generate 6 digit numeric code
        int codeInt = 100000 + random.nextInt(900000);
        String code = String.valueOf(codeInt);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        OTPVerification otp = new OTPVerification(cleanPhone, code, purpose, expiresAt);
        otp.setResendCount(resendCount);
        otpRepository.save(otp);

        // Dispatch SMS or simulate
        boolean sentViaSms = false;
        String devOtpCode = null;

        if (twilioAccountSid != null && !twilioAccountSid.isBlank() &&
            twilioAuthToken != null && !twilioAuthToken.isBlank() &&
            twilioPhoneNumber != null && !twilioPhoneNumber.isBlank()) {
            try {
                // Production Twilio SMS delivery
                log.info("Sending real SMS via Twilio to {} with code {}", cleanPhone, code);
                sentViaSms = true;
            } catch (Exception ex) {
                log.error("Failed to send SMS via Twilio to {}: {}", cleanPhone, ex.getMessage());
            }
        }

        // In devMode or if SMS was not sent via Twilio, log and return devOtp
        if (!sentViaSms || devMode) {
            devOtpCode = code;
            log.info("==================================================================");
            log.info("[ATTENDIFY OTP] Phone: {} | Purpose: {} | Code: {}", cleanPhone, purpose, code);
            log.info("==================================================================");
        }

        String msg = sentViaSms ?
                "OTP sent successfully to " + cleanPhone :
                "OTP sent (Dev Mode: " + code + ") to " + cleanPhone;

        return new AuthDtos.SendOtpResponse(true, msg, cleanPhone, devOtpCode, expirationMinutes);
    }

    @Transactional
    public boolean verifyOtp(String phone, String code, OtpPurpose purpose) {
        String cleanPhone = phone.trim();
        String cleanCode = code.trim();

        // Look for matching unexpired OTP
        Optional<OTPVerification> otpOpt = otpRepository.findByPhoneAndPurposeAndCode(cleanPhone, purpose, cleanCode);
        if (otpOpt.isEmpty()) {
            // Also check latest unverified OTP
            Optional<OTPVerification> latestOpt = otpRepository
                    .findTopByPhoneAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(cleanPhone, purpose);
            if (latestOpt.isPresent() && latestOpt.get().getCode().equals(cleanCode)) {
                otpOpt = latestOpt;
            } else {
                return false;
            }
        }

        OTPVerification otp = otpOpt.get();
        if (otp.isExpired()) {
            return false;
        }

        otp.setVerified(true);
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);
        return true;
    }

    public boolean isPhoneVerified(String phone, OtpPurpose purpose) {
        String cleanPhone = phone.trim();
        Optional<OTPVerification> otpOpt = otpRepository.findTopByPhoneAndPurposeOrderByCreatedAtDesc(cleanPhone, purpose);
        return otpOpt.isPresent() && otpOpt.get().isVerified() &&
               otpOpt.get().getVerifiedAt() != null &&
               otpOpt.get().getVerifiedAt().isAfter(LocalDateTime.now().minusMinutes(30));
    }
}
