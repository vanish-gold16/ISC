package org.example.isc.main.secured.notification;

import jakarta.transaction.Transactional;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.Notification;
import org.example.isc.main.secured.models.Subscription;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.NotificationsRepository;
import org.example.isc.main.secured.repositories.SubscriptionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationsRepository notificationsRepository;
    private final SubscriptionRepository subscriptionRepository;

    public NotificationService(NotificationsRepository notificationsRepository, SubscriptionRepository subscriptionRepository) {
        this.notificationsRepository = notificationsRepository;
        this.subscriptionRepository = subscriptionRepository;
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

    @Transactional
    public void followBack(
            User sender,
            User receiver
    ) {
        if (!subscriptionRepository.existsByFollowedIdAndFollowerId(sender.getId(), receiver.getId())) {
            Subscription subscription = new Subscription(receiver, sender);
            subscriptionRepository.save(subscription);
            String body = " started following you";
            create(
                    NotificationEnum.FOLLOW,
                    sender,
                    receiver,
                    "New follower",
                    body,
                    null
            );
        }
    }

}
