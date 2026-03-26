package org.example.isc.opuscore.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.isc.cloudinary.ImageService;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.service.ReviewService;
import org.example.isc.opuscore.dto.NewReviewDTO;
import org.example.isc.opuscore.models.OpusCoreCriteriaCatalog;
import org.example.isc.opuscore.repositories.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/opuscore")
public class ReviewController {
    private static final String IMAGE_TOO_LARGE_ERROR_CODE = "image-too-large";
    private static final String IMAGE_TOO_LARGE_MESSAGE = "Cover image must be 5 MB or smaller.";

    private final UserRepository userRepository;
    private final ImageService imageService;

    private final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewService reviewService;
    private final OpusCoreCriteriaCatalog criteriaCatalog;
    private final ReviewRepository reviewRepository;

    public ReviewController(
            UserRepository userRepository,
            ImageService imageService,
            ReviewService reviewService,
            OpusCoreCriteriaCatalog criteriaCatalog,
            ReviewRepository reviewRepository
    ) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.reviewService = reviewService;
        this.criteriaCatalog = criteriaCatalog;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/new-review")
    public String getNewReview(
            Authentication authentication,
            Model model,
            @RequestParam(value = "error", required = false) String error
    ){
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
        if (IMAGE_TOO_LARGE_ERROR_CODE.equals(error) && !model.containsAttribute("error")) {
            model.addAttribute("error", IMAGE_TOO_LARGE_MESSAGE);
        }

        return "/opuscore/new-post";
    }

    @GetMapping("/{id}")
    public String getReview(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        var review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));

        model.addAttribute("title", review.getArtName() != null ? review.getArtName() : "OpusCore | Review");
        model.addAttribute("user", me);
        model.addAttribute("review", review);
        model.addAttribute("criteriaScores", review.getCriteriaScores());
        if (review.getUser() != null) {
            model.addAttribute("reviewAuthorId", review.getUser().getId());
            model.addAttribute("reviewAuthorUsername", review.getUser().getUsername());
            String authorAvatarUrl = review.getUser().getProfile() != null && review.getUser().getProfile().getAvatarUrl() != null
                    ? review.getUser().getProfile().getAvatarUrl()
                    : "/images/private/profile/common-profile.png";
            model.addAttribute("reviewAuthorAvatarUrl", authorAvatarUrl);
        }

        return "/opuscore/review";
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
            return getNewReview(authentication, model, null);
        }

        Long review = 0L;

        try{
            review = reviewService.newReview(authentication, form);
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", form);
            return getNewReview(authentication, model, null);
        }
        model.addAttribute("POST_NEW_REVIEW", true);

        log.info("Review created");

        return "redirect:/opuscore/" + review;
    }

    @PostMapping("/new-review/cover")
    @ResponseBody
    public ResponseEntity<String> uploadReviewCover(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        return ResponseEntity.ok(imageService.uploadReviewImage(file, me.getId()));
    }

}
