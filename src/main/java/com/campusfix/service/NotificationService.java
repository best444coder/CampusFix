package com.campusfix.service;

import com.campusfix.model.Notification;
import com.campusfix.model.User;
import com.campusfix.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public void notify(User user, String message) {
        repository.save(Notification.builder()
                .user(user)
                .message(message)
                .readStatus(false)
                .createdAt(LocalDateTime.now())
                .build());
    }

    public List<Notification> getFor(User user) {
        return repository.findByUserOrderByCreatedAtDesc(user);
    }

    public long unread(User user) {
        return repository.countByUserAndReadStatus(user, false);
    }
}
