package org.example.isc.opuscore.controller;

import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.models.users.UserProfile;
import org.example.isc.main.secured.profile.service.ActivityService;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/opuscore/profile")
public class OpusCoreProfileController {

    private static final String PROFILE_IMAGES_BASE = "/images/private/profile/";
    private static final String DEFAULT_AVATAR = "/images/private/profile/common-profile.png";
    private static final String DEFAULT_COVER = "/images/private/profile/common-cover.png";

    private final UserRepository userRepository;
    private final ActivityService activityService;

    public OpusCoreProfileController(UserRepository userRepository, ActivityService activityService) {
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    @GetMapping
    public String getMyProfile(Authentication authentication, Model model) {
        User me = resolveCurrentUser(authentication);
        return renderProfilePage(me, me, model);
    }

    @GetMapping("/{id}")
    public String getProfile(@PathVariable Long id, Authentication authentication, Model model) {
        User me = resolveCurrentUser(authentication);
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
        return renderProfilePage(me, target, model);
    }

    private String renderProfilePage(User me, User target, Model model) {
        boolean isMyProfile = me.getId().equals(target.getId());

        model.addAttribute("title", target.getUsername() != null ? "OpusCore | @" + target.getUsername() : "OpusCore | Profile");
        model.addAttribute("user", me);
        model.addAttribute("profileUser", target);
        model.addAttribute("isMyProfile", isMyProfile);
        model.addAttribute("profileAvatarUrl", resolveAvatarUrl(target));
        model.addAttribute("profileCoverUrl", resolveCoverUrl(target));
        model.addAttribute("profileOnline", activityService.online(target.getId()));

        return "/opuscore/profile";
    }

    private User resolveCurrentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

    private String resolveAvatarUrl(User user) {
        UserProfile profile = user.getProfile();
        if (profile == null) {
            return DEFAULT_AVATAR;
        }
        return normalizeImagePath(profile.getAvatarUrl(), DEFAULT_AVATAR);
    }

    private String resolveCoverUrl(User user) {
        UserProfile profile = user.getProfile();
        if (profile == null) {
            return DEFAULT_COVER;
        }
        return normalizeImagePath(profile.getCoverUrl(), DEFAULT_COVER);
    }

    private String normalizeImagePath(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        String path = value.trim();
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }

        path = path.replace('\\', '/');
        if (!path.startsWith(PROFILE_IMAGES_BASE) || path.contains("..")) {
            return fallback;
        }

        return path;
    }
}
