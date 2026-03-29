package org.example.isc.main.secured.notification;

import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.friends.service.FriendsService;
import org.example.isc.main.secured.models.Notification;
import org.example.isc.main.secured.models.users.Post;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.opuscore.models.Artwork;
import org.example.isc.opuscore.repositories.ArtworkRepository;
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
    private final ArtworkRepository artworkRepository;

    public NotificationController(UserRepository userRepository, NotificationService notificationService, SubscriptionRepository subscriptionRepository, NotificationsRepository notificationsRepository, FriendsService friendsService, PostRepository postRepository, ArtworkRepository artworkRepository) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.subscriptionRepository = subscriptionRepository;
        this.notificationsRepository = notificationsRepository;
        this.friendsService = friendsService;
        this.postRepository = postRepository;
        this.artworkRepository = artworkRepository;
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
        model.addAttribute("artRequestTargets", buildArtRequestTargets(notifications));

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
        if (notification.getReceiver().getId().equals(me.getId()) && notification.getReadAt() == null) {
            notificationService.markRead(id, me);
        }
        if (notification.getType() != null && notification.getType().name().equals("COMMENT")) {
            Long postId = parseId(notification.getData());
            if (postId != null) {
                return "redirect:/posts/" + postId;
            }
        }
        if (notification.getType() != null && notification.getType().name().equals("MESSAGE")) {
            Long conversationId = parseId(notification.getData());
            if (conversationId != null) {
                return "redirect:/messages/c/" + conversationId;
            }
            return "redirect:/messages";
        }
        if (notification.getType() == NotificationEnum.ART_REQUEST_STATUS) {
            String target = resolveArtRequestTarget(notification);
            if (target != null) {
                return "redirect:" + target;
            }
            return "redirect:/notifications";
        }
        if(notification.getSender() != null)
            return "redirect:/profile/" + notification.getSender().getId();

        return "redirect:/notifications";
    }

    private Long parseId(String data) {
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
            Long postId = parseId(n.getData());
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

    private java.util.Map<Long, String> buildArtRequestTargets(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        java.util.Map<Long, String> targets = new java.util.HashMap<>();
        for (Notification notification : notifications) {
            if (notification == null || notification.getNotificationId() == null) {
                continue;
            }
            String target = resolveArtRequestTarget(notification);
            if (target != null) {
                targets.put(notification.getNotificationId(), target);
            }
        }
        return targets;
    }

    private String resolveArtRequestTarget(Notification notification) {
        if (notification == null || notification.getType() != NotificationEnum.ART_REQUEST_STATUS) {
            return null;
        }

        Long artworkId = parseId(notification.getData());
        if (artworkId != null) {
            return "/opuscore/art-page/" + artworkId;
        }

        String artworkName = extractApprovedArtworkName(notification.getBody());
        if (artworkName == null || notification.getReceiver() == null || notification.getReceiver().getId() == null) {
            return null;
        }

        Artwork artwork = artworkRepository
                .findByCreatorIdAndNameIgnoreCaseOrderByCreatedAtDesc(notification.getReceiver().getId(), artworkName)
                .stream()
                .findFirst()
                .orElse(null);

        if (artwork == null || artwork.getId() == null) {
            return null;
        }

        return "/opuscore/art-page/" + artwork.getId();
    }

    private String extractApprovedArtworkName(String body) {
        if (body == null) {
            return null;
        }

        String normalized = body.trim();
        String editedSuffix = " was approved with admin edits.";
        String approvedSuffix = " was approved.";

        String candidate = null;
        if (normalized.endsWith(editedSuffix)) {
            candidate = normalized.substring(0, normalized.length() - editedSuffix.length()).trim();
        } else if (normalized.endsWith(approvedSuffix)) {
            candidate = normalized.substring(0, normalized.length() - approvedSuffix.length()).trim();
        }

        if (candidate == null || candidate.isBlank() || "Your work".equalsIgnoreCase(candidate)) {
            return null;
        }

        return candidate;
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
