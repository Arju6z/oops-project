package com.attendify.controller;

import com.attendify.dto.ApiResponse;
import com.attendify.dto.AuthDtos;
import com.attendify.entity.SchoolClass;
import com.attendify.entity.User;
import com.attendify.entity.enums.OtpPurpose;
import com.attendify.repository.SchoolClassRepository;
import com.attendify.repository.UserRepository;
import com.attendify.security.UserPrincipal;
import com.attendify.service.AuthService;
import com.attendify.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;
    private final SchoolClassRepository classRepository;
    private final UserRepository userRepository;

    public AuthController(AuthService authService,
                          OtpService otpService,
                          SchoolClassRepository classRepository,
                          UserRepository userRepository) {
        this.authService = authService;
        this.otpService = otpService;
        this.classRepository = classRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
        try {
            AuthDtos.AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<AuthDtos.SendOtpResponse>> sendOtp(@Valid @RequestBody AuthDtos.SendOtpRequest request) {
        try {
            OtpPurpose purpose = OtpPurpose.valueOf(request.getPurpose());
            AuthDtos.SendOtpResponse response = otpService.sendOtp(request.getPhone(), purpose);
            return ResponseEntity.ok(ApiResponse.success("OTP dispatched", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid purpose or phone format: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Boolean>> verifyOtp(@Valid @RequestBody AuthDtos.VerifyOtpRequest request) {
        try {
            OtpPurpose purpose = OtpPurpose.valueOf(request.getPurpose());
            boolean valid = otpService.verifyOtp(request.getPhone(), request.getCode(), purpose);
            if (valid) {
                return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", true));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid or expired OTP code"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register-student")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> registerStudent(
            @Valid @RequestBody AuthDtos.StudentRegisterRequest request) {
        try {
            AuthDtos.AuthResponse response = authService.registerStudent(request);
            return ResponseEntity.ok(ApiResponse.success("Student registered successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register-parent")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> registerParent(
            @Valid @RequestBody AuthDtos.ParentRegisterRequest request) {
        try {
            AuthDtos.AuthResponse response = authService.registerParent(request);
            return ResponseEntity.ok(ApiResponse.success("Parent registered successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/register-teacher")
    public ResponseEntity<ApiResponse<AuthDtos.AuthResponse>> registerTeacher(
            @Valid @RequestBody AuthDtos.TeacherRegisterRequest request) {
        try {
            AuthDtos.AuthResponse response = authService.registerTeacher(request);
            return ResponseEntity.ok(ApiResponse.success(
                    "Teacher registration submitted. Please wait for Administrator approval before logging in.", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        User user = userRepository.findById(userPrincipal.getId()).orElse(null);
        return ResponseEntity.ok(ApiResponse.success("User profile", user));
    }

    @GetMapping("/classes")
    public ResponseEntity<ApiResponse<List<SchoolClass>>> getRegistrationClasses() {
        return ResponseEntity.ok(ApiResponse.success("Available classes", classRepository.findAll()));
    }
}
