package org.example.isc.opuscore.service;

import jakarta.transaction.Transactional;
import org.example.isc.cloudinary.ImageService;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewReviewDTO;
import org.example.isc.opuscore.enums.ReviewStatusEnum;
import org.example.isc.opuscore.models.*;
import org.example.isc.opuscore.repositories.ArtworkRepository;
import org.example.isc.opuscore.repositories.NewArtRequestRepository;
import org.example.isc.opuscore.repositories.ReviewRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ReviewService {
    private static final long MAX_REVIEW_IMAGE_BYTES = 5_000_000L;

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final ReviewRepository reviewRepository;
    private final OpusCoreCriteriaCatalog criteriaCatalog;
    private final ArtworkRepository artworkRepository;
    private final NewArtRequestRepository newArtRequestRepository;

    public ReviewService(
            UserRepository userRepository,
            ImageService imageService,
            ReviewRepository reviewRepository,
            OpusCoreCriteriaCatalog criteriaCatalog,
            ArtworkRepository artworkRepository, NewArtRequestRepository newArtRequestRepository) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.reviewRepository = reviewRepository;
        this.criteriaCatalog = criteriaCatalog;
        this.artworkRepository = artworkRepository;
        this.newArtRequestRepository = newArtRequestRepository;
    }

    @Transactional
    public Long newReview(
            Authentication authentication,
            NewReviewDTO form
    ) throws IOException {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));

        if(form.getArtRequestId() != null){
            return newReviewForPendingArt(authentication, form);
        }

        String photoUrl = blankToNull(form.getImageUrl());
        if(photoUrl == null && form.getImage() != null && !form.getImage().isEmpty()) {
            if (form.getImage().getSize() > MAX_REVIEW_IMAGE_BYTES) {
                throw new IllegalArgumentException("Cover image must be 5 MB or smaller.");
            }
            photoUrl = imageService.uploadReviewImage(form.getImage(), me.getId());
        }

        String title = blankToNull(form.getTitle());
        String body = blankToNull(form.getBody());
        if (!form.isReview()) {
            title = null;
            body = null;
        }

        Artwork artwork = artworkRepository.findById(form.getArtworkId())
                .orElseThrow(() -> new IllegalArgumentException("Artwork not found: " + form.getArtworkId()));

        Review review = new Review(
                artwork.getType(),
                form.isReview(),
                artwork,
                title,
                body,
                form.getCriteria()
        );
        review.setUser(me);
        if (review.getCriteriaScores() != null) {
            review.getCriteriaScores().forEach(reviewCriterion -> {
                reviewCriterion.setReview(review);
                if (reviewCriterion.getCriterionId() == null) {
                    throw new IllegalArgumentException("Criterion id is missing");
                }
                var dto = criteriaCatalog.getById(reviewCriterion.getCriterionId());
                reviewCriterion.setName(dto.getName());
                reviewCriterion.setDescription(dto.getDescription());
                reviewCriterion.setWeight(dto.getWeight());
            });
        }
        review.setValue(countScore(review));
        review.setStatus(ReviewStatusEnum.PENDING);
        review.setPhotoUrl(photoUrl);

        reviewRepository.save(review);
        return review.getId();
    }

    public Long newReviewForPendingArt(
            Authentication authentication,
            NewReviewDTO form
    ) throws IOException {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        NewArtRequest request = newArtRequestRepository.findById(form.getArtRequestId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + form.getArtRequestId()));

        if(!request.getStatus().equals(ReviewStatusEnum.PENDING)){
            throw new IllegalArgumentException("This review isn't pending already: " + request);
        }

        if(!request.getRequester().getUsername().equals(me.getUsername())){
            throw new IllegalStateException("There is no this pending art for current user: " + authentication.getName());
        }

        String photoUrl = blankToNull(form.getImageUrl());
        if(photoUrl == null && form.getImage() != null && !form.getImage().isEmpty()) {
            if (form.getImage().getSize() > MAX_REVIEW_IMAGE_BYTES) {
                throw new IllegalArgumentException("Cover image must be 5 MB or smaller.");
            }
            photoUrl = imageService.uploadReviewImage(form.getImage(), me.getId());
        }

        String title = blankToNull(form.getTitle());
        String body = blankToNull(form.getBody());
        if (!form.isReview()) {
            title = null;
            body = null;
        }

        Review review = new Review(
                request.getType(),
                form.isReview(),
                request.getId(),
                request.getName(),
                request.getAuthor(),
                request.getDescription(),
                form.getCriteria()
        );
        review.setUser(me);
        if (review.getCriteriaScores() != null) {
            review.getCriteriaScores().forEach(reviewCriterion -> {
                reviewCriterion.setReview(review);
                if (reviewCriterion.getCriterionId() == null) {
                    throw new IllegalArgumentException("Criterion id is missing");
                }
                var dto = criteriaCatalog.getById(reviewCriterion.getCriterionId());
                reviewCriterion.setName(dto.getName());
                reviewCriterion.setDescription(dto.getDescription());
                reviewCriterion.setWeight(dto.getWeight());
            });
        }
        review.setTitle(title);
        review.setBody(body);
        review.setValue(countScore(review));
        review.setStatus(ReviewStatusEnum.PENDING);
        review.setPhotoUrl(photoUrl);

        reviewRepository.save(review);
        return review.getId();
    }

    private int countScore(Review review){
        int score = 0;

        if (review.getCriteriaScores() == null) {
            return score;
        }

        for (int i = 0; i < review.getCriteriaScores().size(); i++) {
            ReviewCriterion criterion = review.getCriteriaScores().get(i);
            if (criterion == null || criterion.getCriterionId() == null) {
                throw new IllegalArgumentException("Criterion id is missing");
            }
            int weight = criteriaCatalog.getWeightById(criterion.getCriterionId());
            score += criterion.getScore() * weight;
        }

        return Math.max(0, Math.min(100, Math.round(score / 10.0f)));
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
