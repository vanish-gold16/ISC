package org.example.isc.opuscore;

import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewReviewDTO;
import org.example.isc.opuscore.models.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/opuscore")
public class MainController {

    private final UserRepository userRepository;

    private final Logger log = LoggerFactory.getLogger(MainController.class);

    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String getMain(Authentication authentication, Model model){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        model.addAttribute("title", "opuscore");

        return "/opuscore/main";
    }

    @GetMapping("new-post")
    public String getNewPost(Authentication authentication, Model model){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        model.addAttribute("title", "New post");
        model.addAttribute("user", me);
        if(!model.containsAttribute("form")){
            model.addAttribute("form", new NewReviewDTO());
        }

        return "/opuscore/new-post";
    }

}
