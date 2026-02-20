package org.example.isc.main.secured.profile.controller;

import org.example.isc.main.common.dto.EditRequest;
import org.example.isc.main.enums.CountryEnum;
import org.example.isc.main.enums.OccupationEnum;
import org.example.isc.main.secured.models.Subscription;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.UserProfile;
import org.example.isc.main.secured.repositories.PostRepository;
import org.example.isc.main.secured.repositories.SubscriptionRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final String PROFILE_IMAGES_BASE = "/images/private/profile/";
    private static final String DEFAULT_AVATAR = "/images/private/profile/common-profile.png";

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SubscriptionRepository subscriptionRepository;

    public ProfileController(UserRepository userRepository, PostRepository postRepository, SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @GetMapping()
    public String myProfile(Model model, Authentication authentication) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                        .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));

        model.addAttribute("title", "Profile");
        model.addAttribute("user", me);
        model.addAttribute("isMyProfile", true);
        addProfileViewAttributes(model, me);

        model.addAttribute("posts", postRepository.findPostsByUserId(me.getId()));
        model.addAttribute("postsCount", postRepository.findPostsByUserId(me.getId()).size());
        model.addAttribute("followersCount", subscriptionRepository.countByFollowedId((me.getId())));
        model.addAttribute("followingCount", subscriptionRepository.countByFollowerId(((me.getId()))));

        return "private/profile";
    }

    @GetMapping("/{id}")
    public String userProfile(
            @PathVariable Long id, Model model, Authentication authentication
    ){
        User target = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        model.addAttribute("title", "Profile");
        model.addAttribute("user", target);
        model.addAttribute("isMyProfile", me.getId().equals(target.getId()));
        addProfileViewAttributes(model, target);

        model.addAttribute("posts", postRepository.findPostsByUserId(target.getId()));
        model.addAttribute("postsCount", postRepository.findPostsByUserId(target.getId()).size());
        model.addAttribute("followersCount", subscriptionRepository.countByFollowedId((target.getId())));
        model.addAttribute("followingCount", subscriptionRepository.countByFollowerId(((target.getId()))));

        boolean isFollowing = subscriptionRepository
                .findByFollowedIdAndFollowerId(me.getId(), target.getId())
                .isPresent();
        model.addAttribute("isFollowing", isFollowing);

        return "private/user-profile";
    }

    @PostMapping("/{id}/follow")
    private String follow(
            @PathVariable Long id, Authentication authentication
    ){
        User target = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found + " + id));
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not fount " + authentication.getName()));

        if(!me.getId().equals(target.getId())){
            subscriptionRepository.findByFollowedIdAndFollowerId(me.getId(), target.getId())
                    .orElseGet(() -> {
                        Subscription subscription = new Subscription();
                        subscription.setFollower(me);
                        subscription.setFollowed(target);
                        return subscriptionRepository.save(subscription);
                    });
        }

        return "redirect:/profile/" + id;
    }

    @PostMapping("/{id}/unfollow")
    private String unfollow(
            @PathVariable Long id, Authentication authentication
    ){
        User target = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found: " + id));
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        if(!me.getId().equals(target.getId())){
            subscriptionRepository.findByFollowedIdAndFollowerId(me.getId(), target.getId())
                    .ifPresent(subscriptionRepository::delete);
        }

        return "redirect:/profile/" + id;
    }

    @GetMapping("/edit")
    public String edit(Model model, Authentication authentication){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));

        model.addAttribute("title", "Edit profile");
        model.addAttribute("user", me);
        model.addAttribute("profile", me.getProfile());
        model.addAttribute("countries", CountryEnum.values());
        model.addAttribute("occupations", OccupationEnum.values());
        addProfileViewAttributes(model, me);

        return "private/profile-edit";
    }

    @Transactional
    @PostMapping("/edit")
    public String saveEdit(
            EditRequest request,
            Authentication authentication
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));
        String username = request.getUsername();
        String email = request.getEmail();

        if(userRepository.existsByUsername(username) && !authentication.getName().equals(username))
            throw new IllegalArgumentException("This username already exists!");
        if(userRepository.existsByEmail(email) && !me.getEmail().equals(email))
            throw new IllegalArgumentException("This email has already been used!");

        me.setFirstName(request.getFirstName());
        //TODO

        return "redirect:/profile";
    }

    private void addProfileViewAttributes(Model model, User user) {
        UserProfile profile = user.getProfile();

        String avatarUrl = null;
        String coverUrl = null;
        String bio = null;
        String location = null;

        if (profile != null) {
            avatarUrl = normalizeImagePath(profile.getAvatarUrl(), DEFAULT_AVATAR);
            coverUrl = normalizeImagePath(profile.getCoverUrl(), null);
            bio = profile.getBio();

            String city = profile.getCity();
            String country = profile.getCountry() == null ? null : profile.getCountry().name().replace('_', ' ');
            if (city != null && !city.isBlank() && country != null && !country.isBlank()) {
                location = city + ", " + country;
            } else if (city != null && !city.isBlank()) {
                location = city;
            } else if (country != null && !country.isBlank()) {
                location = country;
            }
        }

        model.addAttribute("userAvatarUrl", avatarUrl);
        model.addAttribute("userCoverUrl", coverUrl);
        model.addAttribute("userBio", bio);
        model.addAttribute("userLocation", location);
        model.addAttribute("userJoined", user.getDate());
    }

    private String normalizeImagePath(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        String path = value.trim().replace('\\', '/');
        if (!path.startsWith(PROFILE_IMAGES_BASE) || path.contains("..")) {
            return fallback;
        }
        return path;
    }

}
