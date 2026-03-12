package org.example.isc.opuscore;

import jakarta.transaction.Transactional;
import org.example.isc.cloudinary.ImageService;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewReviewDTO;
import org.example.isc.opuscore.models.Criterion;
import org.example.isc.opuscore.models.Review;
import org.example.isc.opuscore.models.ReviewCriterion;
import org.example.isc.opuscore.repositories.ReviewRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ReviewService {

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final ReviewRepository reviewRepository;

    public ReviewService(UserRepository userRepository, ImageService imageService, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public Long newReview(
            Authentication authentication,
            NewReviewDTO form
    ) throws IOException {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));

        String photoUrl = null;
        if(form.getImage() != null && !form.getImage().isEmpty()) {
            photoUrl = imageService.uploadReviewImage(form.getImage(), me.getId());
        }

        Review review = new Review(
                form.getArtType(),
                form.isReview(),
                form.getName(),
                form.getDescription(),
                form.getTitle(),
                form.getBody(),
                form.getCriteria()
        );
        review.setValue(countScore(review));
        review.setPhotoUrl(photoUrl);

        reviewRepository.save(review);
        return review.getId();
    }

    private int countScore(Review review){
        int score = 0;

        for (int i = 0; i < review.getCriteriaScores().size(); i++) {
            ReviewCriterion criterion = review.getCriteriaScores().get(i);
            Criterion currentCriterion = criterion.getCriterion();
            score += (int) (criterion.getScore()*currentCriterion.getWeight());
        }

        return score;
    }
}
