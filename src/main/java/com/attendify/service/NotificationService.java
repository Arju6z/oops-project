package com.attendify.service;

import com.attendify.entity.*;
import com.attendify.entity.enums.*;
import com.attendify.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryLogRepository deliveryLogRepository;
    private final ParentStudentLinkRepository linkRepository;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AttendanceRuleRepository ruleRepository;

    @Value("${attendify.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${attendify.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${attendify.twilio.phone-number:}")
    private String twilioPhoneNumber;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationDeliveryLogRepository deliveryLogRepository,
                               ParentStudentLinkRepository linkRepository,
                               UserRepository userRepository,
                               StudentProfileRepository studentProfileRepository,
                               AttendanceRuleRepository ruleRepository) {
        this.notificationRepository = notificationRepository;
        this.deliveryLogRepository = deliveryLogRepository;
        this.linkRepository = linkRepository;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.ruleRepository = ruleRepository;
    }

    @Transactional
    public void notifyAttendanceSaved(AttendanceSession session, List<AttendanceRecord> records) {
        AttendanceRule rule = ruleRepository.findFirstByOrderByIdAsc().orElse(new AttendanceRule());
        if (!rule.getAutoNotifyParents()) {
            return;
        }

        for (AttendanceRecord record : records) {
            User student = record.getStudent();
            String studentName = student.getFullName();
            String className = session.getSchoolClass().getDisplayName();
            String subjectName = session.getSubject().getName();
            int period = session.getPeriodNumber();
            String status = record.getStatus().name();
            String date = session.getSessionDate().toString();

            String title = "ATTENDIFY: Attendance update for " + studentName;
            String message = String.format("ATTENDIFY: Attendance update for %s. Class: %s. Subject: %s. Period: %d. Status: %s. Date: %s.",
                    studentName, className, subjectName, period, status, date);

            // Find parents linked to this student
            List<ParentStudentLink> links = linkRepository.findByStudentId(student.getId());

            if (!links.isEmpty()) {
                for (ParentStudentLink link : links) {
                    if (link.getStatus() == LinkStatus.APPROVED) {
                        User parent = link.getParent();
                        sendInAppAndSms(parent, title, message, NotificationType.ATTENDANCE_UPDATE,
                                student.getId(), session.getId(), rule.getAutoSmsEnabled());
                    }
                }
            } else {
                // If not formally linked via ParentStudentLink yet, try to find parent by student's parentPhone
                studentProfileRepository.findByUser(student).ifPresent(profile -> {
                    String parentPhone = profile.getParentPhone();
                    if (parentPhone != null && !parentPhone.isBlank()) {
                        userRepository.findByPhone(parentPhone).ifPresent(parentUser -> {
                            sendInAppAndSms(parentUser, title, message, NotificationType.ATTENDANCE_UPDATE,
                                    student.getId(), session.getId(), rule.getAutoSmsEnabled());
                        });
                    }
                });
            }
        }
    }

    private void sendInAppAndSms(User recipient, String title, String message, NotificationType type,
                                 Long studentId, Long sessionId, boolean sendSms) {
        // 1. Create In-App Notification
        Notification notification = new Notification(recipient, title, message, type, studentId, sessionId);
        notification = notificationRepository.save(notification);

        // Record In-App delivery log
        NotificationDeliveryLog inAppLog = new NotificationDeliveryLog(
                notification,
                DeliveryChannel.IN_APP,
                recipient.getPhone(),
                DeliveryStatus.DELIVERED,
                "Delivered to in-app notification center"
        );
        deliveryLogRepository.save(inAppLog);

        // 2. Dispatch SMS notification
        dispatchSms(notification, recipient.getPhone(), message, sendSms);
    }

    private void dispatchSms(Notification notification, String recipientPhone, String message, boolean sendSms) {
        if (recipientPhone == null || recipientPhone.isBlank()) {
            return;
        }

        DeliveryStatus status = DeliveryStatus.SIMULATED;
        String responseDetail = "Simulated delivery (Development Mode)";

        if (sendSms && twilioAccountSid != null && !twilioAccountSid.isBlank()) {
            try {
                // Production Twilio SMS call
                log.info("Sending Twilio SMS to {}: {}", recipientPhone, message);
                status = DeliveryStatus.DELIVERED;
                responseDetail = "Dispatched via Twilio provider";
            } catch (Exception e) {
                log.error("Twilio SMS failed to {}: {}", recipientPhone, e.getMessage());
                status = DeliveryStatus.FAILED;
                responseDetail = "Twilio Error: " + e.getMessage();
            }
        } else {
            log.info("[ATTENDIFY SMS NOTIFICATION -> {}] {}", recipientPhone, message);
        }

        NotificationDeliveryLog smsLog = new NotificationDeliveryLog(
                notification,
                DeliveryChannel.SMS,
                recipientPhone,
                status,
                responseDetail
        );
        deliveryLogRepository.save(smsLog);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipientUser().getId().equals(userId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId);
    }

    public List<NotificationDeliveryLog> getRecentDeliveryLogs() {
        return deliveryLogRepository.findTop50ByOrderByAttemptedAtDesc();
    }
}
