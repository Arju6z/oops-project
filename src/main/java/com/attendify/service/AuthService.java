package com.attendify.service;

import com.attendify.dto.AuthDtos;
import com.attendify.entity.*;
import com.attendify.entity.enums.LinkStatus;
import com.attendify.entity.enums.OtpPurpose;
import com.attendify.entity.enums.Role;
import com.attendify.entity.enums.UserStatus;
import com.attendify.repository.*;
import com.attendify.security.JwtService;
import com.attendify.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final ParentStudentLinkRepository linkRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository,
                       StudentProfileRepository studentProfileRepository,
                       ParentProfileRepository parentProfileRepository,
                       TeacherProfileRepository teacherProfileRepository,
                       SchoolClassRepository schoolClassRepository,
                       ParentStudentLinkRepository linkRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       OtpService otpService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.parentProfileRepository = parentProfileRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.linkRepository = linkRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.auditService = auditService;
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new BadCredentialsException("Invalid email/phone or password"));

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new IllegalStateException("Your account has been suspended. Please contact the administrator.");
        }

        if (user.getStatus() == UserStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Your account is awaiting administrator approval.");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(userPrincipal);

        AuthDtos.AuthResponse response = new AuthDtos.AuthResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus()
        );

        if (user.getRole() == Role.ROLE_STUDENT) {
            studentProfileRepository.findByUser(user).ifPresent(p -> {
                response.setProfileId(p.getId());
                response.setExtraInfo(p.getAdmissionNumber() + " - " + p.getSchoolClass().getDisplayName());
            });
        } else if (user.getRole() == Role.ROLE_TEACHER) {
            teacherProfileRepository.findByUser(user).ifPresent(p -> {
                response.setProfileId(p.getId());
                response.setExtraInfo(p.getEmployeeId() + " (" + p.getDepartment() + ")");
            });
        }

        return response;
    }

    @Transactional
    public AuthDtos.AuthResponse registerStudent(AuthDtos.StudentRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number is already registered: " + request.getPhone());
        }
        if (studentProfileRepository.existsByAdmissionNumber(request.getAdmissionNumber())) {
            throw new IllegalArgumentException("Admission number already exists: " + request.getAdmissionNumber());
        }

        // Verify Student Phone OTP
        boolean studentOtpValid = otpService.verifyOtp(
                request.getPhone(),
                request.getStudentOtpCode(),
                OtpPurpose.STUDENT_PHONE_VERIFICATION
        );
        if (!studentOtpValid) {
            throw new IllegalArgumentException("Invalid or expired OTP for Student Phone number");
        }

        // Verify Parent Phone OTP
        boolean parentOtpValid = otpService.verifyOtp(
                request.getParentPhone(),
                request.getParentOtpCode(),
                OtpPurpose.STUDENT_PARENT_PHONE_VERIFICATION
        );
        if (!parentOtpValid) {
            throw new IllegalArgumentException("Invalid or expired OTP for Parent Phone number");
        }

        SchoolClass schoolClass = schoolClassRepository.findById(request.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found with id: " + request.getClassId()));

        User userToSave = new User(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_STUDENT,
                UserStatus.ACTIVE
        );
        userToSave.setPhoneVerified(true);
        userToSave.setParentPhoneVerified(true);
        final User user = userRepository.save(userToSave);

        StudentProfile profile = new StudentProfile(
                user,
                request.getAdmissionNumber(),
                schoolClass,
                request.getDivision() != null ? request.getDivision() : schoolClass.getDivision(),
                request.getParentPhone(),
                ""
        );
        studentProfileRepository.save(profile);

        // Check if a parent with this parentPhone is already registered -> link automatically!
        userRepository.findByPhone(request.getParentPhone()).ifPresent(parentUser -> {
            if (parentUser.getRole() == Role.ROLE_PARENT) {
                ParentStudentLink link = new ParentStudentLink(parentUser, user, LinkStatus.APPROVED, "Auto-linked via verified parent phone during registration");
                linkRepository.save(link);
            }
        });

        auditService.log(user, "REGISTER_STUDENT", "User", user.getId().toString(),
                "Student registered: " + user.getFullName() + " (" + request.getAdmissionNumber() + ")", null);

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);

        AuthDtos.AuthResponse response = new AuthDtos.AuthResponse(
                token, user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole(), user.getStatus()
        );
        response.setProfileId(profile.getId());
        response.setExtraInfo(profile.getAdmissionNumber() + " - " + schoolClass.getDisplayName());
        return response;
    }

    @Transactional
    public AuthDtos.AuthResponse registerParent(AuthDtos.ParentRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number is already registered: " + request.getPhone());
        }

        // Verify Parent Phone OTP
        boolean otpValid = otpService.verifyOtp(
                request.getPhone(),
                request.getOtpCode(),
                OtpPurpose.PARENT_PHONE_VERIFICATION
        );
        if (!otpValid) {
            throw new IllegalArgumentException("Invalid or expired OTP for Parent Phone number");
        }

        User userToSave = new User(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_PARENT,
                UserStatus.ACTIVE
        );
        userToSave.setPhoneVerified(true);
        final User user = userRepository.save(userToSave);

        ParentProfile profile = new ParentProfile(
                user,
                request.getAddress(),
                "",
                request.getRelationship() != null ? request.getRelationship() : "Parent"
        );
        parentProfileRepository.save(profile);

        // Link child if admission number was provided
        if (request.getChildAdmissionNumber() != null && !request.getChildAdmissionNumber().isBlank()) {
            studentProfileRepository.findByAdmissionNumber(request.getChildAdmissionNumber().trim()).ifPresent(studentProf -> {
                LinkStatus status = request.getPhone().equals(studentProf.getParentPhone()) ? LinkStatus.APPROVED : LinkStatus.PENDING;
                ParentStudentLink link = new ParentStudentLink(user, studentProf.getUser(), status,
                        status == LinkStatus.APPROVED ? "Auto-verified via matching parent phone" : "Pending admin verification");
                linkRepository.save(link);
            });
        }

        // Also check if any registered students have this parent's phone number
        studentProfileRepository.findByParentPhone(request.getPhone()).forEach(studentProf -> {
            Optional<ParentStudentLink> existing = linkRepository.findByParentIdAndStudentId(user.getId(), studentProf.getUser().getId());
            if (existing.isEmpty()) {
                ParentStudentLink link = new ParentStudentLink(user, studentProf.getUser(), LinkStatus.APPROVED, "Auto-verified matching phone");
                linkRepository.save(link);
            }
        });

        auditService.log(user, "REGISTER_PARENT", "User", user.getId().toString(),
                "Parent registered: " + user.getFullName(), null);

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);

        return new AuthDtos.AuthResponse(
                token, user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole(), user.getStatus()
        );
    }

    @Transactional
    public AuthDtos.AuthResponse registerTeacher(AuthDtos.TeacherRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new IllegalArgumentException("Phone number is already registered: " + request.getPhone());
        }
        if (teacherProfileRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new IllegalArgumentException("Employee ID already registered: " + request.getEmployeeId());
        }

        // Teachers are created with PENDING_APPROVAL status (must be approved by admin)
        User userToSave = new User(
                request.getFullName(),
                request.getEmail(),
                request.getPhone(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_TEACHER,
                UserStatus.PENDING_APPROVAL
        );
        final User user = userRepository.save(userToSave);

        TeacherProfile profile = new TeacherProfile(
                user,
                request.getEmployeeId(),
                request.getDepartment(),
                request.getQualification(),
                false
        );
        teacherProfileRepository.save(profile);

        auditService.log(user, "REGISTER_TEACHER_PENDING", "User", user.getId().toString(),
                "Teacher registered (pending admin approval): " + user.getFullName() + " (" + request.getEmployeeId() + ")", null);

        return new AuthDtos.AuthResponse(
                null, user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), user.getRole(), user.getStatus()
        );
    }
}
