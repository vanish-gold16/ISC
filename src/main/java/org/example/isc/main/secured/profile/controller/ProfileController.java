package org.example.isc.main.secured.profile.controller;

import org.example.isc.main.secured.models.Subscription;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.PostRepository;
import org.example.isc.main.secured.repositories.SubscriptionRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

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

}
