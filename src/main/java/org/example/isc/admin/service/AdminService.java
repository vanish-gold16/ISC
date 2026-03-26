package org.example.isc.admin.service;

import jakarta.transaction.Transactional;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewArtRequestDTO;
import org.example.isc.opuscore.enums.ReviewStatusEnum;
import org.example.isc.opuscore.models.Artwork;
import org.example.isc.opuscore.models.NewArtRequest;
import org.example.isc.opuscore.repositories.ArtworkRepository;
import org.example.isc.opuscore.repositories.NewArtRequestRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final NewArtRequestRepository newArtRequestRepository;
    private final ArtworkRepository artworkRepository;
    private final NotificationService notificationService;

    public AdminService(UserRepository userRepository, NewArtRequestRepository newArtRequestRepository, ArtworkRepository artworkRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.newArtRequestRepository = newArtRequestRepository;
        this.artworkRepository = artworkRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public NewArtRequest approveArtRequest(
            NewArtRequestDTO dto,
            Long id,
            Authentication authentication
    ){
        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        User admin = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Admin not found: " + authentication.getName()));
        User requester = userRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getRequester().getId()));

        Artwork artwork = new Artwork(
                dto.getType(),
                dto.getName(),
                dto.getAuthor(),
                dto.getDescription(),
                dto.getImageUrl(),
                requester,
                admin,
                LocalDateTime.now()
        );

        request.setApprovedArtworkId(artwork.getId());
        request.setDecidedAt(LocalDateTime.now());

        notificationService.create(
                NotificationEnum.REVIEW_STATUS,
                requester,
                admin,
                "Status of your review has been changed",
                request.getName() + " is now: " + request.getStatus() + "ed",
                null
        );

        artworkRepository.save(artwork);

        return newArtRequestRepository.save(request);
    }

    @Transactional
    public NewArtRequest rejectArtRequest(
            String rejectReason,
            Long id,
            Authentication authentication
    ) {
        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        User admin = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Admin not found: " + authentication.getName()));
        User requester = userRepository.findById(request.getRequester().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getRequester().getId()));

        if(rejectReason != null){
            request.setRejectionReason(rejectReason);
        }
        request.setDecidedAt(LocalDateTime.now());

        notificationService.create(
                NotificationEnum.ART_REQUEST_STATUS,
                requester,
                admin,
                "Status of your art request has been changed",
                request.getName() + " is now: " + request.getStatus() + "ed",
                null
        );
        return newArtRequestRepository.save(request);
    }

    @Transactional
    public NewArtRequest changeRequest(
            NewArtRequestDTO dto,
            String adminNote,
            Long id,
            Authentication authentication
    ){
        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        User admin = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Admin not found: " + authentication.getName()));
        User requester = userRepository.findById(request.getRequester().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getRequester().getId()));

        request.setStatus(ReviewStatusEnum.CHANGED);
        request.setType(dto.getType());
        request.setName(dto.getName());
        request.setAuthor(dto.getAuthor());
        request.setDescription(dto.getDescription());
        request.setDecidedAt(LocalDateTime.now());
        if(!adminNote.isBlank()){
            request.setAdminNote(adminNote);
        }
        newArtRequestRepository.save(request);

        notificationService.create(
                NotificationEnum.ART_REQUEST_STATUS,
                requester,
                admin,
                "Status of your art request has been changed",
                request.getName() + " is now: " + request.getStatus() + "ed",
                null
        );

        approveArtRequest(dto, id, authentication);

        return request;
    }
}
