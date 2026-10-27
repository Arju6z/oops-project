package com.attendify.entity;

import com.attendify.entity.enums.DeliveryChannel;
import com.attendify.entity.enums.DeliveryStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_delivery_logs", indexes = {
    @Index(name = "idx_delivery_status", columnList = "status"),
    @Index(name = "idx_delivery_notif", columnList = "notification_id")
})
public class NotificationDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryChannel channel; // IN_APP, SMS, WHATSAPP

    @Column(length = 20)
    private String recipientPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status; // DELIVERED, PENDING, FAILED, SIMULATED

    @Column(length = 500)
    private String providerResponse;

    @Column(nullable = false)
    private LocalDateTime attemptedAt = LocalDateTime.now();

    public NotificationDeliveryLog() {}

    public NotificationDeliveryLog(Notification notification, DeliveryChannel channel, String recipientPhone, DeliveryStatus status, String providerResponse) {
        this.notification = notification;
        this.channel = channel;
        this.recipientPhone = recipientPhone;
        this.status = status;
        this.providerResponse = providerResponse;
        this.attemptedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Notification getNotification() { return notification; }
    public void setNotification(Notification notification) { this.notification = notification; }

    public DeliveryChannel getChannel() { return channel; }
    public void setChannel(DeliveryChannel channel) { this.channel = channel; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public String getProviderResponse() { return providerResponse; }
    public void setProviderResponse(String providerResponse) { this.providerResponse = providerResponse; }

    public LocalDateTime getAttemptedAt() { return attemptedAt; }
    public void setAttemptedAt(LocalDateTime attemptedAt) { this.attemptedAt = attemptedAt; }
}
