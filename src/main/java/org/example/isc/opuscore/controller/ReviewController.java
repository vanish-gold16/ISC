package org.example.isc.opuscore.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.service.ReviewService;
import org.example.isc.opuscore.dto.NewReviewDTO;
import org.example.isc.opuscore.models.OpusCoreCriteriaCatalog;
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
    private final OpusCoreCriteriaCatalog criteriaCatalog;

    public ReviewController(
            UserRepository userRepository,
            ReviewService reviewService,
            OpusCoreCriteriaCatalog criteriaCatalog
    ) {
        this.userRepository = userRepository;
        this.reviewService = reviewService;
        this.criteriaCatalog = criteriaCatalog;
    }

    @GetMapping("/new-review")
    public String getNewReview(Authentication authentication, Model model){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        model.addAttribute("title", "New post");
        model.addAttribute("user", me);
        NewReviewDTO form = (NewReviewDTO) model.asMap().get("form");
        if (form == null) {
            form = new NewReviewDTO();
        }

        if (form.getValue() == null) {
            form.setValue(0L);
        }

        model.addAttribute("form", form);
        model.addAttribute("criteriaByType", criteriaCatalog.getCriteriaByType());

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
        if (bindingResult.hasErrors()) {
            model.addAttribute("form", form);
            return getNewReview(authentication, model);
        }

        Long review = 0L;

        try{
            review = reviewService.newReview(authentication, form);
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", form);
            return getNewReview(authentication, model);
        }
        model.addAttribute("POST_NEW_REVIEW", true);

        log.info("Review created");

        return "redirect:/opuscore/{id}";
    }

}
