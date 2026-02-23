package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.Notification;
import org.example.isc.main.secured.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationsRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecieverOrderByCreatedAtDesc(User reciever, Pageable pageable);

    long countByRecieverAndReadAtIsNull(User reciever);

    @Modifying
    @Query("""
        UPDATE Notification n\s
                   SET n.readAt = CURRENT_TIMESTAMP\s
                   WHERE n.reciever = :reciever AND n.readAt IS NULL
                """)
    void markAllReadByReciever(User reciever);

}
