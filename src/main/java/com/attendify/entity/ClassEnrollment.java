package com.attendify.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "class_enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "class_id", "academic_year"})
})
public class ClassEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(name = "academic_year", nullable = false, length = 30)
    private String academicYear = "2026-2027";

    @Column(length = 20)
    private String rollNumber;

    @Column(nullable = false)
    private boolean active = true;

    public ClassEnrollment() {}

    public ClassEnrollment(User student, SchoolClass schoolClass, String academicYear, String rollNumber) {
        this.student = student;
        this.schoolClass = schoolClass;
        this.academicYear = academicYear;
        this.rollNumber = rollNumber;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public SchoolClass getSchoolClass() { return schoolClass; }
    public void setSchoolClass(SchoolClass schoolClass) { this.schoolClass = schoolClass; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
