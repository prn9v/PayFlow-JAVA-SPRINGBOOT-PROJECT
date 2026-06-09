// NotificationTemplateRepository.java
package com.pranav.notification_service.repository;

import com.pranav.notification_service.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository
        extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByTemplateCodeAndActiveTrue(
            String templateCode);
}