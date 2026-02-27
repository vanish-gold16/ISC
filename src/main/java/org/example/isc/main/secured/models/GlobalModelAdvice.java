package org.example.isc.main.secured.models;

import org.example.isc.main.secured.repositories.NotificationsRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Collections;
import java.util.List;

@ControllerAdvice
public class GlobalModelAdvice {

    private static final String DEFAULT_AVATAR = "/images/private/profile/common-profile.png";

    private final UserRepository userRepository;
    private final NotificationsRepository notificationsRepository;


    public GlobalModelAdvice(UserRepository userRepository, NotificationsRepository notificationsRepository) {
        this.userRepository = userRepository;
        this.notificationsRepository = notificationsRepository;
    }

    @ModelAttribute("userAvatarUrl")
    public String userAvatarUrl(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return DEFAULT_AVATAR;
        }

        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(User::getProfile)
                .map(UserProfile::getAvatarUrl)
                .filter(url -> url != null && !url.isBlank())
                .orElse(DEFAULT_AVATAR);
    }

    @ModelAttribute("unreadNotifications")
    public long unreadNotifications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return 0;
        try {
            return userRepository.findByUsernameIgnoreCase(authentication.getName())
                    .map(notificationsRepository::countByReceiverAndReadAtIsNull)
                    .orElse(0L);
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("notificationsPreview")
    public List<Notification> notificationsPreview(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return Collections.emptyList();
        try {
            return userRepository.findByUsernameIgnoreCase(authentication.getName())
                    .map(user -> notificationsRepository.findByReceiverWithSender(user, PageRequest.of(0, 5)))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

}
