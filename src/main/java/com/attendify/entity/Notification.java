package com.attendify.entity;

import com.attendify.entity.enums.NotificationType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_recipient", columnList = "recipient_user_id"),
    @Index(name = "idx_notif_created", columnList = "createdAt")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipientUser;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type = NotificationType.ATTENDANCE_UPDATE;

    @Column(nullable = false)
    private boolean isRead = false;

    private Long relatedStudentId;
    private Long relatedSessionId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}

    public Notification(User recipientUser, String title, String message, NotificationType type, Long relatedStudentId, Long relatedSessionId) {
        this.recipientUser = recipientUser;
        this.title = title;
        this.message = message;
        this.type = type;
        this.relatedStudentId = relatedStudentId;
        this.relatedSessionId = relatedSessionId;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getRecipientUser() { return recipientUser; }
    public void setRecipientUser(User recipientUser) { this.recipientUser = recipientUser; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Long getRelatedStudentId() { return relatedStudentId; }
    public void setRelatedStudentId(Long relatedStudentId) { this.relatedStudentId = relatedStudentId; }

    public Long getRelatedSessionId() { return relatedSessionId; }
    public void setRelatedSessionId(Long relatedSessionId) { this.relatedSessionId = relatedSessionId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
