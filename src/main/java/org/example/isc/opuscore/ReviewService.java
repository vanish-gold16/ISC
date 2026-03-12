package org.example.isc.opuscore;

import jakarta.transaction.Transactional;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewReviewDTO;
import org.example.isc.opuscore.repositories.ReviewRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final UserRepository userRepository;

    public ReviewService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void newPost(
            Authentication authentication,
            NewReviewDTO form
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));


    }

}
