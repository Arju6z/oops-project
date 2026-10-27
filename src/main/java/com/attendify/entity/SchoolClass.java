package com.attendify.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "classes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "division"})
})
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // e.g. "Grade 10" or "B.Tech CSE 3rd Year"

    @Column(nullable = false, length = 20)
    private String division; // e.g. "A", "B", "Section 1"

    @Column(length = 50)
    private String roomNumber;

    @Column(length = 30)
    private String academicYear = "2026-2027";

    public SchoolClass() {}

    public SchoolClass(String name, String division, String roomNumber, String academicYear) {
        this.name = name;
        this.division = division;
        this.roomNumber = roomNumber;
        this.academicYear = academicYear;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getDisplayName() {
        return name + " (" + division + ")";
    }
}
