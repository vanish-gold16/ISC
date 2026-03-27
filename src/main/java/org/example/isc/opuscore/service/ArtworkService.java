package org.example.isc.opuscore.service;

import jakarta.transaction.Transactional;
import org.example.isc.cloudinary.ImageService;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewArtRequestDTO;
import org.example.isc.opuscore.enums.ReviewStatusEnum;
import org.example.isc.opuscore.models.NewArtRequest;
import org.example.isc.opuscore.repositories.NewArtRequestRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class ArtworkService {

    private static final long MAX_ART_IMAGE_BYTES = 5_000_000L;

    private final UserRepository userRepository;
    private final ImageService imageService;
    private final NewArtRequestRepository newArtRequestRepository;

    public ArtworkService(UserRepository userRepository, ImageService imageService, NewArtRequestRepository newArtRequestRepository) {
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.newArtRequestRepository = newArtRequestRepository;
    }

    @Transactional
    public Long newArtRequest(
            Authentication authentication,
            NewArtRequestDTO dto
    ) throws IOException {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        String photoUrl = blankToNull(dto.getImageUrl());

        if(photoUrl == null && dto.getImage() != null && !dto.getImage().isEmpty()){
            if(dto.getImage().getSize() > MAX_ART_IMAGE_BYTES){
                throw new IllegalArgumentException("Cover image must be 5 MB or smaller.");
            }
            photoUrl = imageService.uploadReviewImage(dto.getImage(), me.getId());
        }

        String name = blankToNull(dto.getName());
        String author = blankToNull(dto.getAuthor());
        String description = blankToNull(dto.getDescription());

        NewArtRequest request = new NewArtRequest(
                ReviewStatusEnum.PENDING,
                dto.getType(),
                name,
                author,
                description,
                LocalDateTime.now()
        );
        request.setRequester(me);
        request.setCoverUrl(photoUrl);

        newArtRequestRepository.save(request);


        return request.getId();
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
