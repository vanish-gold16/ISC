package org.example.isc.opuscore.controller;

import jakarta.validation.Valid;
import org.example.isc.cloudinary.ImageService;
import org.example.isc.main.enums.RoleEnum;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.models.Artwork;
import org.example.isc.opuscore.models.NewArtRequest;
import org.example.isc.opuscore.models.Review;
import org.example.isc.opuscore.models.ReviewCriterion;
import org.example.isc.opuscore.repositories.ArtworkRepository;
import org.example.isc.opuscore.repositories.NewArtRequestRepository;
import org.example.isc.opuscore.service.ReviewService;
import org.example.isc.opuscore.dto.NewReviewDTO;
import org.example.isc.opuscore.dto.CriterionDTO;
import org.example.isc.opuscore.dto.ReviewCriterionViewDTO;
import org.example.isc.opuscore.models.OpusCoreCriteriaCatalog;
import org.example.isc.opuscore.repositories.ReviewRepository;
import org.example.isc.opuscore.enums.ReviewStatusEnum;
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
import java.util.List;
import java.util.Optional;

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
    private final ArtworkRepository artworkRepository;
    private final NewArtRequestRepository newArtRequestRepository;

    public ReviewController(
            UserRepository userRepository,
            ImageService imageService,
            ReviewService reviewService,
            OpusCoreCriteriaCatalog criteriaCatalog,
            ReviewRepository reviewRepository,
            ArtworkRepository artworkRepository,
            NewArtRequestRepository newArtRequestRepository) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.reviewService = reviewService;
        this.criteriaCatalog = criteriaCatalog;
        this.reviewRepository = reviewRepository;
        this.artworkRepository = artworkRepository;
        this.newArtRequestRepository = newArtRequestRepository;
    }

    @GetMapping("/new-review")
    public String getNewReview(
            Authentication authentication,
            Model model,
            @RequestParam(value = "artId", required = false) Long artId,
            @RequestParam(value = "artRequestId", required = false) Long artRequestId,
            @RequestParam(value = "error", required = false) String error
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        model.addAttribute("title", "New review");
        model.addAttribute("user", me);
        NewReviewDTO form = (NewReviewDTO) model.asMap().get("form");
        if (form == null) {
            form = new NewReviewDTO();
        }

        if (form.getValue() == null) {
            form.setValue(0L);
        }

        Long selectedArtworkId = form.getArtworkId() != null ? form.getArtworkId() : artId;
        Long selectedArtRequestId = form.getArtRequestId() != null ? form.getArtRequestId() : artRequestId;

        if (selectedArtRequestId != null) {
            String redirect = populatePendingRequestPrefill(me, form, model, selectedArtRequestId);
            if (redirect != null) {
                return redirect;
            }
            selectedArtworkId = form.getArtworkId();
            selectedArtRequestId = form.getArtRequestId();
        } else if (selectedArtworkId != null) {
            populateArtworkPrefill(form, model, selectedArtworkId);
        }

        Optional<Review> existingReview = reviewService.findExistingReview(me.getId(), selectedArtworkId, selectedArtRequestId);
        if (existingReview.isPresent()) {
            return "redirect:/opuscore/" + existingReview.get().getId();
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

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));

        Artwork artwork = review.getArtwork();

        model.addAttribute("title", "OpusCore | Review");
        model.addAttribute("user", me);
        model.addAttribute("review", review);
        model.addAttribute("criteriaScores", buildCriteriaView(review));
        if (review.getUser() != null) {
            model.addAttribute("reviewAuthorId", review.getUser().getId());
            model.addAttribute("reviewAuthorUsername", review.getUser().getUsername());
            String authorAvatarUrl = review.getUser().getProfile() != null && review.getUser().getProfile().getAvatarUrl() != null
                    ? review.getUser().getProfile().getAvatarUrl()
                    : "/images/private/profile/common-profile.png";
            model.addAttribute("reviewAuthorAvatarUrl", authorAvatarUrl);
        }
        model.addAttribute("artwork", artwork);

        return "/opuscore/review";
    }

    @GetMapping("/{id}/edit")
    public String getEditReview(
        @PathVariable Long id,
        Authentication authentication,
        Model model
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));

        if (me.getId() != review.getUser().getId()){
            throw new IllegalStateException("Wrong user for review");
        }

        model.addAttribute("user", me);
        model.addAttribute("review", review);

        return "/opuscore/edit-review";
    }

    @PatchMapping("/{id}/edit")
    public String editReview(
        @PathVariable Long id,
        @Valid @ModelAttribute("form") NewReviewDTO form,
        Authentication authentication
    ) throws IOException {
        if (form == null) {
            throw new IllegalArgumentException("Form is empty");
        }
        reviewService.editReview(form, authentication, id);

        return "redirect:/opuscore/review";
    }

    @PostMapping("/new-review")
    public String postNewReview(
            @Valid @ModelAttribute("form") NewReviewDTO form,
            BindingResult bindingResult,
            Model model,
            Authentication authentication
    ){
        if (bindingResult.hasErrors()) {
            model.addAttribute("form", form);
            return getNewReview(authentication, model, null, null, null);
        }

        Long review = 0L;

        try{
            review = reviewService.newReview(authentication, form);
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("form", form);
            return getNewReview(authentication, model, null, null, null);
        }
        model.addAttribute("POST_NEW_REVIEW", true);

        log.info("Review created");

        return "redirect:/opuscore/" + review;
    }

    @PostMapping("/new-review/cover")
    @ResponseBody
    public ResponseEntity<String> uploadArtCover(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        return ResponseEntity.ok(imageService.uploadReviewImage(file, me.getId()));
    }

    private String populatePendingRequestPrefill(
            User me,
            NewReviewDTO form,
            Model model,
            Long artRequestId
    ) {
        NewArtRequest request = newArtRequestRepository.findById(artRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Art request not found: " + artRequestId));

        boolean isOwner = request.getRequester() != null
                && request.getRequester().getId() != null
                && request.getRequester().getId().equals(me.getId());
        boolean isAdmin = me.getRole() == RoleEnum.ADMIN;

        if (!isOwner && !isAdmin) {
            return "redirect:/opuscore";
        }

        if (request.getStatus() == ReviewStatusEnum.REJECTED) {
            return "redirect:/opuscore";
        }

        if (request.getApprovedArtworkId() != null) {
            populateArtworkPrefill(form, model, request.getApprovedArtworkId());
            return null;
        }

        form.setArtworkId(null);
        form.setArtRequestId(request.getId());
        form.setArtType(request.getType());
        populateSelectedArtModel(model, request.getName(), request.getAuthor(), request.getDescription(), request.getCoverUrl());
        return null;
    }

    private void populateArtworkPrefill(
            NewReviewDTO form,
            Model model,
            Long artworkId
    ) {
        Artwork artwork = artworkRepository.findById(artworkId)
                .orElseThrow(() -> new IllegalArgumentException("Artwork not found: " + artworkId));

        form.setArtworkId(artwork.getId());
        form.setArtRequestId(null);
        form.setArtType(artwork.getType());
        populateSelectedArtModel(model, artwork.getName(), artwork.getAuthor(), artwork.getDescription(), artwork.getCoverUrl());
    }

    private void populateSelectedArtModel(
            Model model,
            String name,
            String author,
            String description,
            String coverUrl
    ) {
        model.addAttribute("selectedArtName", name);
        model.addAttribute("selectedArtAuthor", author);
        model.addAttribute("selectedArtDescription", description);
        model.addAttribute("selectedArtCoverUrl", coverUrl);
    }

    private List<ReviewCriterionViewDTO> buildCriteriaView(Review review) {
        if (review == null || review.getCriteriaScores() == null || review.getCriteriaScores().isEmpty()) {
            return List.of();
        }

        return review.getCriteriaScores().stream()
                .map(criterion -> toView(review, criterion))
                .toList();
    }

    private ReviewCriterionViewDTO toView(Review review, ReviewCriterion criterion) {
        CriterionDTO catalogCriterion = criteriaCatalog.findByTypeAndName(review.getType(), criterion.getName())
                .orElse(null);

        String name = firstNonBlank(
                criterion.getName(),
                catalogCriterion != null ? catalogCriterion.getName() : null,
                "Criterion"
        );
        String description = firstNonBlank(
                criterion.getDescription(),
                catalogCriterion != null ? catalogCriterion.getDescription() : null,
                "Description is not available yet."
        );
        int weight = criterion.getWeight() > 0
                ? criterion.getWeight()
                : (catalogCriterion != null ? catalogCriterion.getWeight() : 0);

        return new ReviewCriterionViewDTO(name, description, criterion.getScore(), weight);
    }

    private String firstNonBlank(String first, String second, String fallback) {
        String firstValue = blankToNull(first);
        if (firstValue != null) {
            return firstValue;
        }

        String secondValue = blankToNull(second);
        if (secondValue != null) {
            return secondValue;
        }

        return fallback;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
