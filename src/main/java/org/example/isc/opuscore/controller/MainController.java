package org.example.isc.opuscore.controller;

import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.repositories.ReviewRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/opuscore")
public class MainController {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public MainController(UserRepository userRepository, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public String getMain(Authentication authentication, Model model){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        model.addAttribute("title", "opuscore");

        return "/opuscore/main";
    }

    @GetMapping("/my-ratings")
    public String getMyRatings(Authentication authentication, Model model){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        model.addAttribute("title", "My Ratings");
        model.addAttribute("ratings", reviewRepository.findByUserIdOrderByValueDescIdDesc(me.getId()));

        return "/opuscore/my-ratings";
    }

}
