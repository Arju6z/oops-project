package com.attendify.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects", indexes = {
    @Index(name = "idx_subject_code", columnList = "code", unique = true)
})
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // e.g. "Mathematics", "Operating Systems"

    @Column(nullable = false, unique = true, length = 20)
    private String code; // e.g. "MATH101", "CS302"

    @Column(length = 255)
    private String description;

    public Subject() {}

    public Subject(String name, String code, String description) {
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
