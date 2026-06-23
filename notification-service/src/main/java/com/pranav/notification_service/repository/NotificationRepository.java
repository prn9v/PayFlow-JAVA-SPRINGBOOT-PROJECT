package com.pranav.notification_service.repository;

import com.pranav.notification_service.entity.Notification;
import com.pranav.notification_service.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReferenceId(String referenceId);

    List<Notification> findByRecipientEmail(String recipientEmail);

    List<Notification> findByStatus(NotificationStatus status);

    @Query("""
            SELECT n FROM Notification n
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(n.recipientEmail) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(n.subject)        LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(n.referenceId)    LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL
                   OR n.status = :status)
            """)
    Page<Notification> findAllWithFilters(
            @Param("search") String search,
            @Param("status") NotificationStatus status,
            Pageable pageable);
}