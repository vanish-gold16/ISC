package org.example.isc.main.common;

import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

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
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        return notificationService.unreadCount(me);
    }
}

