package org.example.isc.opuscore;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewReviewDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/opuscore")
public class ReviewController {

    private final UserRepository userRepository;

    private final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewService reviewService;

    public ReviewController(UserRepository userRepository, ReviewService reviewService) {
        this.userRepository = userRepository;
        this.reviewService = reviewService;
    }

    @GetMapping("/new-review")
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

    @PostMapping("/new-review")
    public String postNewReview(
            @Valid @ModelAttribute("form") NewReviewDTO form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            Authentication authentication
    ){
        if(bindingResult.hasErrors()) return getNewPost(authentication, model);

        try{
            reviewService.newReview(authentication, form);
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            return getNewPost(authentication, model);
        }
        model.addAttribute("POST_NEW_REVIEW", true);

        return "redirect:/opuscore/{id}";
    }

}
