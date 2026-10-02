package com.campusfix.repository;

import com.campusfix.model.Notification;
import com.campusfix.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    long countByUserAndReadStatus(User user, boolean readStatus);
}
