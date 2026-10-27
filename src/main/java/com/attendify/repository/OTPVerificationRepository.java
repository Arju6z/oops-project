package com.attendify.repository;

import com.attendify.entity.OTPVerification;
import com.attendify.entity.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OTPVerificationRepository extends JpaRepository<OTPVerification, Long> {
    Optional<OTPVerification> findTopByPhoneAndPurposeOrderByCreatedAtDesc(String phone, OtpPurpose purpose);
    Optional<OTPVerification> findTopByPhoneAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(String phone, OtpPurpose purpose);
    Optional<OTPVerification> findByPhoneAndPurposeAndCode(String phone, OtpPurpose purpose, String code);
}
