package com.attendify.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "attendance_rules")
public class AttendanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double minimumAttendancePercentage = 75.0;

    @Column(nullable = false)
    private Double lateMultiplier = 1.0; // 1.0 = counts as full present, 0.5 = counts as half present

    @Column(nullable = false)
    private Integer correctionWindowHours = 48; // Teacher can correct within 48h

    @Column(nullable = false)
    private Boolean autoNotifyParents = true;

    @Column(nullable = false)
    private Boolean autoSmsEnabled = false;

    public AttendanceRule() {}

    public AttendanceRule(Double minimumAttendancePercentage, Double lateMultiplier, Integer correctionWindowHours, Boolean autoNotifyParents, Boolean autoSmsEnabled) {
        this.minimumAttendancePercentage = minimumAttendancePercentage;
        this.lateMultiplier = lateMultiplier;
        this.correctionWindowHours = correctionWindowHours;
        this.autoNotifyParents = autoNotifyParents;
        this.autoSmsEnabled = autoSmsEnabled;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getMinimumAttendancePercentage() { return minimumAttendancePercentage; }
    public void setMinimumAttendancePercentage(Double minimumAttendancePercentage) { this.minimumAttendancePercentage = minimumAttendancePercentage; }

    public Double getLateMultiplier() { return lateMultiplier; }
    public void setLateMultiplier(Double lateMultiplier) { this.lateMultiplier = lateMultiplier; }

    public Integer getCorrectionWindowHours() { return correctionWindowHours; }
    public void setCorrectionWindowHours(Integer correctionWindowHours) { this.correctionWindowHours = correctionWindowHours; }

    public Boolean getAutoNotifyParents() { return autoNotifyParents; }
    public void setAutoNotifyParents(Boolean autoNotifyParents) { this.autoNotifyParents = autoNotifyParents; }

    public Boolean getAutoSmsEnabled() { return autoSmsEnabled; }
    public void setAutoSmsEnabled(Boolean autoSmsEnabled) { this.autoSmsEnabled = autoSmsEnabled; }
}
