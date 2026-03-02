package org.example.isc.main.secured.notification;

import org.example.isc.main.secured.friends.service.FriendsService;
import org.example.isc.main.secured.models.Notification;
import org.example.isc.main.secured.models.Post;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.PostRepository;
import org.example.isc.main.secured.repositories.NotificationsRepository;
import org.example.isc.main.secured.repositories.SubscriptionRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
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
    private final PostRepository postRepository;

    public NotificationController(UserRepository userRepository, NotificationService notificationService, SubscriptionRepository subscriptionRepository, NotificationsRepository notificationsRepository, FriendsService friendsService, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.subscriptionRepository = subscriptionRepository;
        this.notificationsRepository = notificationsRepository;
        this.friendsService = friendsService;
        this.postRepository = postRepository;
    }

    @GetMapping("/notifications")
    public String notifications(
            Model model,
            Authentication authentication
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                        .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        List<Notification> notifications = notificationService.list(me, 20);

        List<Notification> unreadNotificationsList = notifications.stream()
                .filter(n -> n.getReadAt() == null)
                .collect(Collectors.toList());
        List<Notification> readNotificationsList = notifications.stream()
                .filter(n -> n.getReadAt() != null)
                .collect(Collectors.toList());

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
        model.addAttribute("unreadNotificationsList", unreadNotificationsList);
        model.addAttribute("readNotificationsList", readNotificationsList);
        model.addAttribute("commentPostPreviews", buildCommentPostPreviews(notifications));

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
        notificationService.markRead(id, me);
        currentNotification.setReadAt(LocalDateTime.now());

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
        notificationService.markRead(id, me);
        currentNotification.setReadAt(LocalDateTime.now());

        return "redirect:/notifications";
    }

    @PostMapping("/notifications/{id}/cancel-friend-request")
    public String cancelFriendRequest(
            @PathVariable Long id,
            Authentication authentication
    ){
        Notification currentNotification = notificationsRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Notification not found: " + id));
        User sender = currentNotification.getSender();
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        friendsService.cancelFriendRequest(sender, me);
        notificationService.markRead(id, me);
        currentNotification.setReadAt(LocalDateTime.now());

        return "redirect:/notifications";
    }

    @GetMapping("/notifications/{id}/open")
    public String openNotification(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        Notification notification = notificationsRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Notification not found: " + id));
        if(notification.getReceiver().getId().equals(me.getId())
        && notification.getReadAt() == null
        )
            notification.setReadAt(LocalDateTime.now());
        if (notification.getType() != null && notification.getType().name().equals("COMMENT")) {
            Long postId = parsePostId(notification.getData());
            if (postId != null) {
                return "redirect:/posts/" + postId;
            }
        }
        if(notification.getSender() != null)
            return "redirect:/profile/" + notification.getSender().getId();

        return "redirect:/notifications";
    }

    private Long parsePostId(String data) {
        if (data == null || data.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(data.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private java.util.Map<Long, NotificationPostPreview> buildCommentPostPreviews(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        java.util.Map<Long, NotificationPostPreview> previews = new java.util.HashMap<>();
        for (Notification n : notifications) {
            if (n == null || n.getType() == null || !n.getType().name().equals("COMMENT")) {
                continue;
            }
            Long postId = parsePostId(n.getData());
            if (postId == null) {
                continue;
            }
            Post post = postRepository.findById(postId).orElse(null);
            if (post == null) {
                continue;
            }
            previews.put(n.getNotificationId(), new NotificationPostPreview(post.getId(), post.getPhotoUrl()));
        }
        return previews;
    }

    public static class NotificationPostPreview {
        private final Long postId;
        private final String photoUrl;

        public NotificationPostPreview(Long postId, String photoUrl) {
            this.postId = postId;
            this.photoUrl = photoUrl;
        }

        public Long getPostId() {
            return postId;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }
    }
}
