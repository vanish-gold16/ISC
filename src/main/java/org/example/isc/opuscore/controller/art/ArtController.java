package org.example.isc.opuscore.controller.art;

import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.models.Artwork;
import org.example.isc.opuscore.service.ReviewService;
import org.example.isc.opuscore.repositories.ArtworkRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/opuscore/art-page")
public class ArtController {

    private final ArtworkRepository artworkRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;

    public ArtController(ArtworkRepository artworkRepository, UserRepository userRepository, ReviewService reviewService) {
        this.artworkRepository = artworkRepository;
        this.userRepository = userRepository;
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public String getArt(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ){
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artwork not found: " + id));
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        model.addAttribute("title", "Artwork");
        model.addAttribute("user", me);
        model.addAttribute("artwork", artwork);
        reviewService.findExistingReview(me.getId(), artwork.getId(), null)
                .ifPresent(review -> model.addAttribute("existingReview", review));

        return "/opuscore/art-page";
    }

}
