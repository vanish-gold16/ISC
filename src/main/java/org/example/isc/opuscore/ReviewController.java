package org.example.isc.opuscore;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewReviewDTO;
import org.example.isc.opuscore.models.Criterion;
import org.example.isc.opuscore.models.Review;
import org.example.isc.opuscore.models.ReviewCriterion;
import org.example.isc.opuscore.repositories.CriterionRepository;
import org.example.isc.opuscore.repositories.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/opuscore")
public class ReviewController {

    private final UserRepository userRepository;

    private final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final CriterionRepository criterionRepository;

    public ReviewController(
            UserRepository userRepository,
            ReviewService reviewService,
            ReviewRepository reviewRepository,
            CriterionRepository criterionRepository
    ) {
        this.userRepository = userRepository;
        this.reviewService = reviewService;
        this.reviewRepository = reviewRepository;
        this.criterionRepository = criterionRepository;
    }

    @GetMapping("/new-review")
    public String getNewPost(Authentication authentication, Model model){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        model.addAttribute("title", "New post");
        model.addAttribute("user", me);
        NewReviewDTO form = (NewReviewDTO) model.asMap().get("form");
        if (form == null) {
            form = new NewReviewDTO();
        }

        if (form.getCriteria() == null || form.getCriteria().isEmpty()) {
            // finding by the ArtTypeEnum
            List<Criterion> criteria = criterionRepository.findAll();
            form.setCriteria(criteria.stream().map(criterion -> {
                ReviewCriterion reviewCriterion = new ReviewCriterion();
                reviewCriterion.setCriterion(criterion);
                reviewCriterion.setScore(5);
                return reviewCriterion;
            }).toList());
        }

        if (form.getValue() == null) {
            form.setValue(0L);
        }

        model.addAttribute("form", form);

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
            return getNewPost(authentication, model);
        }

        Long review = 0L;

        try{
            review = reviewService.newReview(authentication, form);
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", form);
            return getNewPost(authentication, model);
        }
        model.addAttribute("POST_NEW_REVIEW", true);

        log.info("Review created");

        addModelAttributes(model, form, review);

        return "redirect:/opuscore/{id}";
    }

    private void addModelAttributes(Model model, NewReviewDTO form, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalStateException("Review not found: " + reviewId));
        List<ReviewCriterion> criteriaScores = review.getCriteriaScores();
        for (int i = 0; i < criteriaScores.size(); i++) {
            model.addAttribute("criterionName", criteriaScores.get(i).getCriterion().getName());
            model.addAttribute("criterionDescription", criteriaScores.get(i).getCriterion().getDescription());
            model.addAttribute("score", review.getValue());
        }
    }

}
