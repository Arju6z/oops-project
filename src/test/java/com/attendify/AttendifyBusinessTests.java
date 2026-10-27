package com.attendify;

import com.attendify.dto.ApiResponse;
import com.attendify.dto.AttendanceDtos;
import com.attendify.dto.AuthDtos;
import com.attendify.dto.ReportDtos;
import com.attendify.entity.*;
import com.attendify.entity.enums.*;
import com.attendify.repository.*;
import com.attendify.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AttendifyBusinessTests {

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchoolClassRepository classRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private AttendanceRecordRepository recordRepository;

    @Autowired
    private AttendanceSessionRepository sessionRepository;

    @Test
    @DisplayName("Test 1: OTP Generation and Verification Workflow")
    void testOtpGenerationAndVerification() {
        String testPhone = "9988776655";
        OtpPurpose purpose = OtpPurpose.STUDENT_PHONE_VERIFICATION;

        AuthDtos.SendOtpResponse sendResp = otpService.sendOtp(testPhone, purpose);
        assertNotNull(sendResp);
        assertTrue(sendResp.isSuccess());
        assertNotNull(sendResp.getDevOtp(), "Dev OTP should be populated in development mode");

        // Verify invalid code fails
        boolean invalidVerify = otpService.verifyOtp(testPhone, "000000", purpose);
        assertFalse(invalidVerify);

        // Verify correct code succeeds
        boolean validVerify = otpService.verifyOtp(testPhone, sendResp.getDevOtp(), purpose);
        assertTrue(validVerify);
    }

    @Test
    @DisplayName("Test 2: Attendance Duplicate Prevention Constraint")
    @Transactional
    void testDuplicateAttendancePrevention() {
        User teacher = userRepository.findByEmail("teacher.jenkins@attendify.edu").orElseThrow();
        SchoolClass sc = classRepository.findAll().get(0);
        Subject math = subjectRepository.findByCode("MATH101").orElseThrow();

        LocalDate testDate = LocalDate.now().minusDays(15); // unique past date
        int periodNumber = 6;

        AttendanceDtos.MarkAttendanceRequest req = new AttendanceDtos.MarkAttendanceRequest();
        req.setClassId(sc.getId());
        req.setSubjectId(math.getId());
        req.setDate(testDate);
        req.setPeriodNumber(periodNumber);
        req.setRecords(List.of(
                new AttendanceDtos.StudentAttendanceItem(teacher.getId(), AttendanceStatus.PRESENT, "Test")
        ));

        // First submission should succeed
        AttendanceDtos.AttendanceSessionDto firstResult = attendanceService.markAttendance(teacher.getId(), req);
        assertNotNull(firstResult);

        // Second submission for the same class, subject, date, and period MUST throw IllegalStateException
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            attendanceService.markAttendance(teacher.getId(), req);
        });
        assertTrue(exception.getMessage().contains("already been recorded"));
    }

    @Test
    @DisplayName("Test 3: Attendance Percentage Calculation Accuracy")
    void testAttendancePercentageCalculation() {
        User student = userRepository.findByEmail("aarav.sharma@attendify.edu").orElseThrow();
        double pct = attendanceService.calculateStudentPercentage(student.getId());

        assertTrue(pct >= 0.0 && pct <= 100.0, "Percentage should be between 0 and 100");
    }

    @Test
    @DisplayName("Test 4: Class Attendance Report and CSV Export")
    void testReportAndCsvGeneration() {
        SchoolClass sc = classRepository.findAll().get(0);
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();

        ReportDtos.ClassReportDto report = reportService.generateClassReport(sc.getId(), start, end);
        assertNotNull(report);
        assertEquals(sc.getId(), report.getClassId());
        assertFalse(report.getStudents().isEmpty(), "Class should contain enrolled students in report");

        String csv = reportService.generateClassReportCsv(sc.getId(), start, end);
        assertNotNull(csv);
        assertTrue(csv.contains("ATTENDIFY - Attendance Summary Report"));
        assertTrue(csv.contains("Roll No,Admission No,Student Name"));
    }

    @Test
    @DisplayName("Test 5: Password Login Authentication")
    void testPasswordLogin() {
        AuthDtos.LoginRequest req = new AuthDtos.LoginRequest();
        req.setIdentifier("admin@attendify.edu");
        req.setPassword("Admin@123");

        AuthDtos.AuthResponse resp = authService.login(req);
        assertNotNull(resp);
        assertNotNull(resp.getToken(), "JWT token must be returned on valid login");
        assertEquals(Role.ROLE_ADMIN, resp.getRole());
    }
}
