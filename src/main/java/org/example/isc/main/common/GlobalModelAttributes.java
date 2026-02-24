package org.example.isc.main.common;

import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.Notification;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public GlobalModelAttributes(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @ModelAttribute("unreadNotifications")
    public Long unreadNotifications(Authentication authentication){
        if (!isAuthenticated(authentication)) {
            return 0L;
        }

        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        return notificationService.unreadCount(me);
    }

    @ModelAttribute("notificationsPreview")
    public List<Notification> notificationsPreview(Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return Collections.emptyList();
        }

        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        return notificationService.list(me, 5);
    }
}
