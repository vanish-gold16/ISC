package org.example.isc.main.secured.notification;

import jakarta.transaction.Transactional;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.Notification;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.NotificationsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationsRepository notificationsRepository;

    public NotificationService(NotificationsRepository notificationsRepository) {
        this.notificationsRepository = notificationsRepository;
    }

    public void create(
            NotificationEnum type,
            User receiver,
            User sender,
            String title,
            String body,
            String data
                       ){
        Notification notification = new Notification(
                receiver,
                sender,
                type,
                title,
                body,
                LocalDateTime.now(),
                null,
                data
        );
        notificationsRepository.save(notification);
    }

    public List<Notification> list(User receiver, int limit){
        Pageable pageable = PageRequest.of(0, limit);
        return notificationsRepository.findByReceiverWithSender(receiver, pageable);
    }

    public long unreadCount(User receiver){
        return notificationsRepository.countByReceiverAndReadAtIsNull(receiver);
    }

    @Transactional
    public void markRead(Long notificationId, User receiver){
        notificationsRepository.markReadByNotificationIdAndReceiver(notificationId, receiver);
    }

    @Transactional
    public void markAllRead(User receiver){
        notificationsRepository.markAllReadByReceiver(receiver);
    }

}
