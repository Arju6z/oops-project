package com.attendify.dto;

import com.attendify.entity.enums.Role;
import com.attendify.entity.enums.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public static class LoginRequest {
        @NotBlank(message = "Email or phone is required")
        private String identifier;

        @NotBlank(message = "Password is required")
        private String password;

        public LoginRequest() {}

        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AuthResponse {
        private String token;
        private Long id;
        private String fullName;
        private String email;
        private String phone;
        private Role role;
        private UserStatus status;
        private Long profileId;
        private String extraInfo;

        public AuthResponse() {}

        public AuthResponse(String token, Long id, String fullName, String email, String phone, Role role, UserStatus status) {
            this.token = token;
            this.id = id;
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.status = status;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public Role getRole() { return role; }
        public void setRole(Role role) { this.role = role; }

        public UserStatus getStatus() { return status; }
        public void setStatus(UserStatus status) { this.status = status; }

        public Long getProfileId() { return profileId; }
        public void setProfileId(Long profileId) { this.profileId = profileId; }

        public String getExtraInfo() { return extraInfo; }
        public void setExtraInfo(String extraInfo) { this.extraInfo = extraInfo; }
    }

    public static class SendOtpRequest {
        @NotBlank(message = "Phone number is required")
        private String phone;

        @NotBlank(message = "OTP purpose is required")
        private String purpose;

        public SendOtpRequest() {}

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
    }

    public static class SendOtpResponse {
        private boolean success;
        private String message;
        private String phone;
        private String devOtp; // Populated in development mode for easy UI testing
        private int expiresInMinutes;

        public SendOtpResponse(boolean success, String message, String phone, String devOtp, int expiresInMinutes) {
            this.success = success;
            this.message = message;
            this.phone = phone;
            this.devOtp = devOtp;
            this.expiresInMinutes = expiresInMinutes;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getPhone() { return phone; }
        public String getDevOtp() { return devOtp; }
        public int getExpiresInMinutes() { return expiresInMinutes; }
    }

    public static class VerifyOtpRequest {
        @NotBlank(message = "Phone number is required")
        private String phone;

        @NotBlank(message = "OTP Code is required")
        private String code;

        @NotBlank(message = "OTP Purpose is required")
        private String purpose;

        public VerifyOtpRequest() {}

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getPurpose() { return purpose; }
        public void setPurpose(String purpose) { this.purpose = purpose; }
    }

    public static class StudentRegisterRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Phone is required")
        private String phone;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Admission number is required")
        private String admissionNumber;

        @NotNull(message = "Class is required")
        private Long classId;

        private String division;

        @NotBlank(message = "Parent phone is required")
        private String parentPhone;

        @NotBlank(message = "Student phone OTP verification code is required")
        private String studentOtpCode;

        @NotBlank(message = "Parent phone OTP verification code is required")
        private String parentOtpCode;

        public StudentRegisterRequest() {}

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getAdmissionNumber() { return admissionNumber; }
        public void setAdmissionNumber(String admissionNumber) { this.admissionNumber = admissionNumber; }

        public Long getClassId() { return classId; }
        public void setClassId(Long classId) { this.classId = classId; }

        public String getDivision() { return division; }
        public void setDivision(String division) { this.division = division; }

        public String getParentPhone() { return parentPhone; }
        public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

        public String getStudentOtpCode() { return studentOtpCode; }
        public void setStudentOtpCode(String studentOtpCode) { this.studentOtpCode = studentOtpCode; }

        public String getParentOtpCode() { return parentOtpCode; }
        public void setParentOtpCode(String parentOtpCode) { this.parentOtpCode = parentOtpCode; }
    }

    public static class ParentRegisterRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Phone is required")
        private String phone;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        private String address;
        private String relationship;

        @NotBlank(message = "Phone OTP verification code is required")
        private String otpCode;

        private String childAdmissionNumber; // Optional admission number to link directly

        public ParentRegisterRequest() {}

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getRelationship() { return relationship; }
        public void setRelationship(String relationship) { this.relationship = relationship; }

        public String getOtpCode() { return otpCode; }
        public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

        public String getChildAdmissionNumber() { return childAdmissionNumber; }
        public void setChildAdmissionNumber(String childAdmissionNumber) { this.childAdmissionNumber = childAdmissionNumber; }
    }

    public static class TeacherRegisterRequest {
        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Phone is required")
        private String phone;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "Employee ID is required")
        private String employeeId;

        private String department;
        private String qualification;

        public TeacherRegisterRequest() {}

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getQualification() { return qualification; }
        public void setQualification(String qualification) { this.qualification = qualification; }
    }
}
