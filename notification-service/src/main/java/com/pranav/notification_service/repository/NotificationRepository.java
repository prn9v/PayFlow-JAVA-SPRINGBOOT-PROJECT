// NotificationRepository.java
package com.pranav.notification_service.repository;

import com.pranav.notification_service.entity.Notification;
import com.pranav.notification_service.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByReferenceId(String referenceId);

    List<Notification> findByRecipientEmail(String recipientEmail);

    List<Notification> findByStatus(NotificationStatus status);
}