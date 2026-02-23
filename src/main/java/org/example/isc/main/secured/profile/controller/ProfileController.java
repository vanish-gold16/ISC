package org.example.isc.main.secured.profile.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.isc.main.common.dto.EditRequest;
import org.example.isc.main.enums.CountryEnum;
import org.example.isc.main.enums.OccupationEnum;
import org.example.isc.main.secured.models.Subscription;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.UserProfile;
import org.example.isc.main.secured.profile.service.ProfileService;
import org.example.isc.main.secured.repositories.PostRepository;
import org.example.isc.main.secured.repositories.SubscriptionRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final String PROFILE_IMAGES_BASE = "/images/private/profile/";
    private static final String DEFAULT_AVATAR = "/images/private/profile/common-profile.png";
    private static final String DEFAULT_COVER = "/images/private/profile/common-cover.png";

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ProfileService profileService;

    public ProfileController(UserRepository userRepository, PostRepository postRepository, SubscriptionRepository subscriptionRepository, ProfileService profileService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.profileService = profileService;
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

        return "private/profile/profile";
    }

    @GetMapping("/{id}")
    public String userProfile(
            @PathVariable Long id, Model model, Authentication authentication
    ){
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));
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
                .findByFollowedIdAndFollowerId(target.getId(), me.getId())
                .isPresent();
        model.addAttribute("isFollowing", isFollowing);

        return "private/profile/user-profile";
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
            if (!subscriptionRepository.existsByFollowedIdAndFollowerId(target.getId(), me.getId())) {
                Subscription subscription = new Subscription();
                subscription.setFollower(me);
                subscription.setFollowed(target);
                subscriptionRepository.save(subscription);
            }
        }

        return "redirect:/profile/" + id;
    }

    @PostMapping("/{id}/unfollow")
    private String unfollow(
            @PathVariable Long id, Authentication authentication
    ){
        subscriptionRepository.deleteAll(
                subscriptionRepository.findAllByFollowedIdAndFollowerUsernameIgnoreCase(id, authentication.getName())
        );

        return "redirect:/profile/" + id;
    }

    @GetMapping("/edit")
    public String edit(Model model, Authentication authentication){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));
        UserProfile profile = me.getProfile();
        EditRequest form = new EditRequest();
        form.setFirstName(me.getFirstName());
        form.setLastName(me.getLastName());
        form.setUsername(me.getUsername());
        form.setEmail(me.getEmail());
        if (profile != null) {
            form.setBio(profile.getBio());
            form.setCountry(profile.getCountry());
            form.setCity(profile.getCity());
            form.setCurrentStudy(profile.getCurrentStudy());
            form.setOccupationEnum(profile.getOccupationEnum());
            form.setBirthDate(profile.getBirthDate());
        }

        model.addAttribute("title", "Edit profile");
        model.addAttribute("form", form);
        model.addAttribute("user", me);
        model.addAttribute("profile", profile);
        model.addAttribute("countries", CountryEnum.values());
        model.addAttribute("occupations", OccupationEnum.values());
        addProfileViewAttributes(model, me);

        return "private/profile/profile-edit";
    }

    @PostMapping("/edit")
    public String saveEdit(
            @Valid @ModelAttribute("form") EditRequest request,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            Authentication authentication
    ) {
        if(bindingResult.hasErrors()) {
            User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                    .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));
            model.addAttribute("title", "Edit profile");
            model.addAttribute("user", me);
            model.addAttribute("profile", me.getProfile());
            model.addAttribute("countries", CountryEnum.values());
            model.addAttribute("occupations", OccupationEnum.values());
            addProfileViewAttributes(model, me);
            return "private/profile/profile-edit";
        }
        try {
            profileService.edit(userRepository, authentication, request);
        } catch(IllegalArgumentException e){
            bindingResult.rejectValue("username", "username.exists", e.getMessage());
            User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                    .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));
            model.addAttribute("title", "Edit profile");
            model.addAttribute("user", me);
            model.addAttribute("profile", me.getProfile());
            model.addAttribute("countries", CountryEnum.values());
            model.addAttribute("occupations", OccupationEnum.values());
            addProfileViewAttributes(model, me);
            return "private/profile/profile-edit";
        }

        session.setAttribute("POST_EDIT_PROFILE", true);

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
            coverUrl = normalizeImagePath(profile.getCoverUrl(), DEFAULT_COVER);
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
        model.addAttribute("userCoverUrl", coverUrl == null ? DEFAULT_COVER : coverUrl);
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
