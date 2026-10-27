package com.attendify.config;

import com.attendify.entity.*;
import com.attendify.entity.enums.*;
import com.attendify.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ParentProfileRepository parentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherAssignmentRepository assignmentRepository;
    private final TimetableRepository timetableRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final ParentStudentLinkRepository linkRepository;
    private final AttendanceRuleRepository ruleRepository;
    private final AcademicYearRepository academicYearRepository;
    private final HolidayRepository holidayRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           StudentProfileRepository studentProfileRepository,
                           ParentProfileRepository parentProfileRepository,
                           TeacherProfileRepository teacherProfileRepository,
                           SchoolClassRepository classRepository,
                           SubjectRepository subjectRepository,
                           TeacherAssignmentRepository assignmentRepository,
                           TimetableRepository timetableRepository,
                           AttendanceSessionRepository sessionRepository,
                           AttendanceRecordRepository recordRepository,
                           ParentStudentLinkRepository linkRepository,
                           AttendanceRuleRepository ruleRepository,
                           AcademicYearRepository academicYearRepository,
                           HolidayRepository holidayRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.parentProfileRepository = parentProfileRepository;
        this.teacherProfileRepository = teacherProfileRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.assignmentRepository = assignmentRepository;
        this.timetableRepository = timetableRepository;
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
        this.linkRepository = linkRepository;
        this.ruleRepository = ruleRepository;
        this.academicYearRepository = academicYearRepository;
        this.holidayRepository = holidayRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data loading.");
            return;
        }

        log.info("Initializing ATTENDIFY seed data...");

        // 1. Rules & Academic Year
        AttendanceRule rules = new AttendanceRule(75.0, 1.0, 48, true, false);
        ruleRepository.save(rules);

        AcademicYear academicYear = new AcademicYear("2026-2027", LocalDate.of(2026, 6, 1), LocalDate.of(2027, 5, 31), true);
        academicYearRepository.save(academicYear);

        Holiday holiday = new Holiday("Independence Day / Campus Recess", LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 15), "National holiday");
        holidayRepository.save(holiday);

        // 2. Classes
        SchoolClass class10A = classRepository.save(new SchoolClass("Grade 10", "A", "Room 101", "2026-2027"));
        SchoolClass class10B = classRepository.save(new SchoolClass("Grade 10", "B", "Room 102", "2026-2027"));
        SchoolClass class12A = classRepository.save(new SchoolClass("Grade 12", "Science", "Lab 201", "2026-2027"));

        // 3. Subjects
        Subject math = subjectRepository.save(new Subject("Mathematics", "MATH101", "Advanced Algebra and Trigonometry"));
        Subject physics = subjectRepository.save(new Subject("Physics", "PHYS101", "Mechanics and Waves"));
        Subject compSci = subjectRepository.save(new Subject("Computer Science", "CS101", "Data Structures and Algorithms"));
        Subject english = subjectRepository.save(new Subject("English Literature", "ENG101", "Contemporary Prose and Poetry"));

        // 4. Administrator
        User admin = new User("Dr. Robert Vance (Principal)", "admin@attendify.edu", "9876543210",
                passwordEncoder.encode("Admin@123"), Role.ROLE_ADMIN, UserStatus.ACTIVE);
        admin.setPhoneVerified(true);
        userRepository.save(admin);

        // 5. Teachers
        User teacher1 = new User("Prof. Sarah Jenkins", "teacher.jenkins@attendify.edu", "9876543211",
                passwordEncoder.encode("Teacher@123"), Role.ROLE_TEACHER, UserStatus.ACTIVE);
        teacher1.setPhoneVerified(true);
        teacher1 = userRepository.save(teacher1);
        TeacherProfile teacherProfile1 = new TeacherProfile(teacher1, "EMP-T-101", "Mathematics", "M.Sc., B.Ed.", true);
        teacherProfile1.setApprovedAt(LocalDateTime.now());
        teacherProfile1.setApprovedBy("System Administrator");
        teacherProfileRepository.save(teacherProfile1);

        User teacher2 = new User("Dr. Alan Turing", "teacher.turing@attendify.edu", "9876543212",
                passwordEncoder.encode("Teacher@123"), Role.ROLE_TEACHER, UserStatus.ACTIVE);
        teacher2.setPhoneVerified(true);
        teacher2 = userRepository.save(teacher2);
        TeacherProfile teacherProfile2 = new TeacherProfile(teacher2, "EMP-T-102", "Computer Science", "Ph.D. Computer Science", true);
        teacherProfile2.setApprovedAt(LocalDateTime.now());
        teacherProfile2.setApprovedBy("System Administrator");
        teacherProfileRepository.save(teacherProfile2);

        // Teacher 3 (Pending approval demo)
        User teacher3 = new User("Markus Drake (Applicant)", "teacher.drake@attendify.edu", "9876543213",
                passwordEncoder.encode("Teacher@123"), Role.ROLE_TEACHER, UserStatus.PENDING_APPROVAL);
        teacher3 = userRepository.save(teacher3);
        TeacherProfile teacherProfile3 = new TeacherProfile(teacher3, "EMP-T-103", "Physics", "M.Sc. Physics", false);
        teacherProfileRepository.save(teacherProfile3);

        // Teacher Assignments
        assignmentRepository.save(new TeacherAssignment(teacher1, class10A, math, "2026-2027"));
        assignmentRepository.save(new TeacherAssignment(teacher1, class10B, math, "2026-2027"));
        assignmentRepository.save(new TeacherAssignment(teacher2, class10A, compSci, "2026-2027"));
        assignmentRepository.save(new TeacherAssignment(teacher2, class12A, compSci, "2026-2027"));

        // Timetable entries
        timetableRepository.save(new Timetable(class10A, math, teacher1, DayOfWeekEnum.MONDAY, 1, LocalTime.of(9, 0), LocalTime.of(9, 45)));
        timetableRepository.save(new Timetable(class10A, compSci, teacher2, DayOfWeekEnum.MONDAY, 2, LocalTime.of(9, 45), LocalTime.of(10, 30)));
        timetableRepository.save(new Timetable(class10A, math, teacher1, DayOfWeekEnum.TUESDAY, 1, LocalTime.of(9, 0), LocalTime.of(9, 45)));
        timetableRepository.save(new Timetable(class10A, compSci, teacher2, DayOfWeekEnum.WEDNESDAY, 3, LocalTime.of(10, 45), LocalTime.of(11, 30)));

        // 6. Parents
        User parent1 = new User("Rajesh Sharma", "rajesh.sharma@attendify.edu", "9876543230",
                passwordEncoder.encode("Parent@123"), Role.ROLE_PARENT, UserStatus.ACTIVE);
        parent1.setPhoneVerified(true);
        parent1 = userRepository.save(parent1);
        parentProfileRepository.save(new ParentProfile(parent1, "42 Magnolia Boulevard, North Wing", "Architect", "Father"));

        User parent2 = new User("Meera Patel", "meera.patel@attendify.edu", "9876543231",
                passwordEncoder.encode("Parent@123"), Role.ROLE_PARENT, UserStatus.ACTIVE);
        parent2.setPhoneVerified(true);
        parent2 = userRepository.save(parent2);
        parentProfileRepository.save(new ParentProfile(parent2, "17 Palm Grove Avenue", "Senior Scientist", "Mother"));

        // 7. Students
        // Student 1: Aarav Sharma (Child 1 of Rajesh Sharma)
        User student1 = new User("Aarav Sharma", "aarav.sharma@attendify.edu", "9876543220",
                passwordEncoder.encode("Student@123"), Role.ROLE_STUDENT, UserStatus.ACTIVE);
        student1.setPhoneVerified(true);
        student1.setParentPhoneVerified(true);
        student1 = userRepository.save(student1);
        StudentProfile sp1 = studentProfileRepository.save(
                new StudentProfile(student1, "ADM-2026-001", class10A, "A", "9876543230", "101"));

        // Student 2: Diya Patel (Child of Meera Patel)
        User student2 = new User("Diya Patel", "diya.patel@attendify.edu", "9876543221",
                passwordEncoder.encode("Student@123"), Role.ROLE_STUDENT, UserStatus.ACTIVE);
        student2.setPhoneVerified(true);
        student2.setParentPhoneVerified(true);
        student2 = userRepository.save(student2);
        StudentProfile sp2 = studentProfileRepository.save(
                new StudentProfile(student2, "ADM-2026-002", class10A, "A", "9876543231", "102"));

        // Student 3: Rohan Sharma (Child 2 of Rajesh Sharma - multi-child demo)
        User student3 = new User("Rohan Sharma", "rohan.sharma@attendify.edu", "9876543222",
                passwordEncoder.encode("Student@123"), Role.ROLE_STUDENT, UserStatus.ACTIVE);
        student3.setPhoneVerified(true);
        student3.setParentPhoneVerified(true);
        student3 = userRepository.save(student3);
        StudentProfile sp3 = studentProfileRepository.save(
                new StudentProfile(student3, "ADM-2026-003", class10A, "A", "9876543230", "103"));

        // Student 4: Kavya Nair (Low attendance demo student)
        User student4 = new User("Kavya Nair", "kavya.nair@attendify.edu", "9876543223",
                passwordEncoder.encode("Student@123"), Role.ROLE_STUDENT, UserStatus.ACTIVE);
        student4.setPhoneVerified(true);
        student4.setParentPhoneVerified(true);
        student4 = userRepository.save(student4);
        StudentProfile sp4 = studentProfileRepository.save(
                new StudentProfile(student4, "ADM-2026-004", class10A, "A", "9876543232", "104"));

        // Student 5: Liam Wilson
        User student5 = new User("Liam Wilson", "liam.wilson@attendify.edu", "9876543224",
                passwordEncoder.encode("Student@123"), Role.ROLE_STUDENT, UserStatus.ACTIVE);
        student5.setPhoneVerified(true);
        student5.setParentPhoneVerified(true);
        student5 = userRepository.save(student5);
        StudentProfile sp5 = studentProfileRepository.save(
                new StudentProfile(student5, "ADM-2026-005", class10A, "A", "9876543233", "105"));

        // 8. Link Parent to Students
        linkRepository.save(new ParentStudentLink(parent1, student1, LinkStatus.APPROVED, "Verified father"));
        linkRepository.save(new ParentStudentLink(parent1, student3, LinkStatus.APPROVED, "Verified father (second child)"));
        linkRepository.save(new ParentStudentLink(parent2, student2, LinkStatus.APPROVED, "Verified mother"));

        // 9. Historical Attendance Data for the past 10 days
        LocalDate today = LocalDate.now();
        LocalDate[] pastDates = new LocalDate[]{
                today.minusDays(9),
                today.minusDays(8),
                today.minusDays(7),
                today.minusDays(6),
                today.minusDays(5),
                today.minusDays(4),
                today.minusDays(3),
                today.minusDays(2),
                today.minusDays(1),
                today
        };

        for (int i = 0; i < pastDates.length; i++) {
            LocalDate d = pastDates[i];
            if (d.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) continue;

            // Session 1: Math Period 1
            AttendanceSession s1 = new AttendanceSession(class10A, math, teacher1, d, 1, SessionStatus.SUBMITTED, "Regular class session");
            s1 = sessionRepository.save(s1);

            recordRepository.save(new AttendanceRecord(s1, student1, AttendanceStatus.PRESENT, ""));
            recordRepository.save(new AttendanceRecord(s1, student2, AttendanceStatus.PRESENT, ""));
            recordRepository.save(new AttendanceRecord(s1, student3, i % 4 == 0 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT, i % 4 == 0 ? "Bus delay" : ""));
            // Student 4 has frequent absences
            recordRepository.save(new AttendanceRecord(s1, student4, i % 2 == 0 ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT, i % 2 == 0 ? "Absent without prior leave" : ""));
            recordRepository.save(new AttendanceRecord(s1, student5, AttendanceStatus.PRESENT, ""));

            // Session 2: CS Period 2
            AttendanceSession s2 = new AttendanceSession(class10A, compSci, teacher2, d, 2, SessionStatus.SUBMITTED, "Lab & problem solving");
            s2 = sessionRepository.save(s2);

            recordRepository.save(new AttendanceRecord(s2, student1, AttendanceStatus.PRESENT, ""));
            recordRepository.save(new AttendanceRecord(s2, student2, i % 5 == 0 ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT, ""));
            recordRepository.save(new AttendanceRecord(s2, student3, AttendanceStatus.PRESENT, ""));
            recordRepository.save(new AttendanceRecord(s2, student4, AttendanceStatus.ABSENT, "Absent"));
            recordRepository.save(new AttendanceRecord(s2, student5, AttendanceStatus.PRESENT, ""));
        }

        log.info("ATTENDIFY seed data initialization complete!");
        log.info("Default Demo Credentials:");
        log.info("  Admin:   admin@attendify.edu          / Admin@123");
        log.info("  Teacher: teacher.jenkins@attendify.edu / Teacher@123");
        log.info("  Parent:  rajesh.sharma@attendify.edu   / Parent@123");
        log.info("  Student: aarav.sharma@attendify.edu    / Student@123");
    }
}
