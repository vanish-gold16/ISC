package org.example.isc.opuscore.controller.api;

import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.art.ArtDropdownDTO;
import org.example.isc.opuscore.models.Artwork;
import org.example.isc.opuscore.repositories.ArtworkRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/opuscore/api/artworks")
public class ArtApiController {

    private final UserRepository userRepository;
    private final ArtworkRepository artworkRepository;

    public ArtApiController(UserRepository userRepository, ArtworkRepository artworkRepository) {
        this.userRepository = userRepository;
        this.artworkRepository = artworkRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<List<ArtDropdownDTO>> getDropdown(
            @RequestParam(value = "query", required = false) String query,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        String normalizedQuery = normalize(query);
        List<Artwork> arts = normalizedQuery != null
                ? artworkRepository.findByResolvedName(normalizedQuery)
                : List.of();

        return ResponseEntity.ok(arts.stream().map(this::toDTO).toList());
    }

    private ArtDropdownDTO toDTO(Artwork artwork){
        return new ArtDropdownDTO(
                artwork.getId(),
                artwork.getName(),
                artwork.getAuthor(),
                artwork.getDescription(),
                artwork.getType(),
                artwork.getCoverUrl()
        );
    }

    private User requireCurrentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

}
