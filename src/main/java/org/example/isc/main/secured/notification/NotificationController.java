package org.example.isc.main.secured.notification;

import org.example.isc.main.secured.friends.service.FriendsService;
import org.example.isc.main.secured.models.Notification;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.NotificationsRepository;
import org.example.isc.main.secured.repositories.SubscriptionRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class NotificationController {

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationsRepository notificationsRepository;
    private final FriendsService friendsService;

    public NotificationController(UserRepository userRepository, NotificationService notificationService, SubscriptionRepository subscriptionRepository, NotificationsRepository notificationsRepository, FriendsService friendsService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.subscriptionRepository = subscriptionRepository;
        this.notificationsRepository = notificationsRepository;
        this.friendsService = friendsService;
    }

    @GetMapping("/notifications")
    public String notifications(
            Model model,
            Authentication authentication
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                        .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        List<Notification> notifications = notificationService.list(me, 20);

        // Собираем ID отправителей уведомлений
        Set<Long> senderIds = notifications.stream()
                .filter(n -> n.getSender() != null)
                .map(n -> n.getSender().getId())
                .collect(Collectors.toSet());

        // Проверяем, на кого из них мы уже подписаны
        Set<Long> followingIds = senderIds.stream()
                .filter(senderId -> subscriptionRepository.existsByFollowedIdAndFollowerId(senderId, me.getId()))
                .collect(Collectors.toSet());

        model.addAttribute("title", "Notifications");
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", notificationService.unreadCount(me));
        model.addAttribute("followingIds", followingIds != null ? followingIds : Collections.emptySet());
        model.addAttribute("currentUserId", me.getId());

        return "/private/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String read(
            @PathVariable Long id,
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                        .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        notificationService.markRead(id, me);

        return "redirect:/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String readAll(
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        notificationService.markAllRead(me);

        return "redirect:/notifications";
    }

    @PostMapping("/notifications/{id}/follow-back")
    public String followBack(
            @PathVariable Long id,
            Authentication authentication
    ){
        Notification currentNotification = notificationsRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Notification not found: " + id));
        User sender = currentNotification.getSender();
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        notificationService.followBack(sender, me);

        return "redirect:/notifications";
    }

    @PostMapping("/notifications/{id}/accept-friend-request")
    public String acceptFriendRequest(
            @PathVariable Long id,
            Authentication authentication
    ){
        Notification currentNotification = notificationsRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Notification not found: " + id));
        User sender = currentNotification.getSender();
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        friendsService.acceptFriendRequest(sender, me);

        return "redirect:/notifications";
    }

}
