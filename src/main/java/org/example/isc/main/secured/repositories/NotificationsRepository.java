package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.Notification;
import org.example.isc.main.secured.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationsRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecieverOrderByCreatedAtDesc(User reciever, Pageable pageable);

    long countByRecieverAndReadAtIsNull(User reciever, LocalDateTime readAt);

    void markAllReadByReciever(User reciever);

}
