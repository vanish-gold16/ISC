package org.example.isc.main.secured.notification;

import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.Notification;
import org.example.isc.main.secured.models.Post;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.NotificationsRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationsRepository notificationsRepository;
    private final Pageable pageable;

    public NotificationService(NotificationsRepository notificationsRepository, Pageable pageable) {
        this.notificationsRepository = notificationsRepository;
        this.pageable = pageable;
    }

    public void create(
            NotificationEnum type,
            User receiver,
            String title,
            String body,
            String data
                       ){
        Notification notification = new Notification(
                receiver,
                type,
                title,
                body,
                LocalDateTime.now(),
                null,
                data
        );
    }

    public List<Notification> list(User receiver, int limit){
        return notificationsRepository.findByReceiverOrderByCreatedAtDesc(receiver, pageable);
    }

    public long unreadCount(User receiver){
        return notificationsRepository.countByRecieverAndReadAtIsNull(receiver);
    }

    public void markRead(Long notificationId, User receiver){
        notificationsRepository.markReadByNotificationIdAndReciever(notificationId, receiver);
    }

    public void markAllRead(User receiver){
        notificationsRepository.markAllReadByReciever(receiver);
    }

}
