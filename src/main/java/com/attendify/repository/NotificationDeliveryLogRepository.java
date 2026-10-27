package com.attendify.repository;

import com.attendify.entity.NotificationDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, Long> {
    List<NotificationDeliveryLog> findByNotificationId(Long notificationId);
    List<NotificationDeliveryLog> findTop50ByOrderByAttemptedAtDesc();
}
