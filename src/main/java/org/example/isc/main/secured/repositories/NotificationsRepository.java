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

    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver, Pageable pageable);

    long countByRecieverAndReadAtIsNull(User receiver);

    @Modifying
    @Query("""
        UPDATE Notification n
                   SET n.readAt = CURRENT_TIMESTAMP
                   WHERE n.receiver = :receiver AND n.readAt IS NULL
                """)
    void markAllReadByReciever(User receiver);

    @Modifying
    @Query("""
          update Notification n
                    set n.readAt = current_timestamp 
                    where n.receiver = :receiver 
                    and n.notificationId = :notificationId
                    and n.readAt is null          
           """)
    void markReadByNotificationIdAndReciever(Long notificationId, User receiver);

}
