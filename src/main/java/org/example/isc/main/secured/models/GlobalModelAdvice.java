package org.example.isc.main.secured.models;

import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private static final String DEFAULT_AVATAR = "/images/private/profile/common-profile.png";

    private final UserRepository userRepository;


    public GlobalModelAdvice(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
