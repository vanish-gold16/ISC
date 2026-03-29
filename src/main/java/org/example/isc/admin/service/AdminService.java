package org.example.isc.admin.service;

import jakarta.transaction.Transactional;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.NewArtRequestDTO;
import org.example.isc.opuscore.dto.NewReviewDTO;
import org.example.isc.opuscore.dto.RejectDTO;
import org.example.isc.opuscore.enums.ReviewStatusEnum;
import org.example.isc.opuscore.models.Artwork;
import org.example.isc.opuscore.models.NewArtRequest;
import org.example.isc.opuscore.models.Review;
import org.example.isc.opuscore.repositories.ArtworkRepository;
import org.example.isc.opuscore.repositories.NewArtRequestRepository;
import org.example.isc.opuscore.repositories.ReviewRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final NewArtRequestRepository newArtRequestRepository;
    private final ArtworkRepository artworkRepository;
    private final NotificationService notificationService;
    private final ReviewRepository reviewRepository;

    public AdminService(UserRepository userRepository, NewArtRequestRepository newArtRequestRepository, ArtworkRepository artworkRepository, NotificationService notificationService, ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.newArtRequestRepository = newArtRequestRepository;
        this.artworkRepository = artworkRepository;
        this.notificationService = notificationService;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public NewArtRequest approveArtRequest(
            NewArtRequestDTO dto,
            Long id,
            Authentication authentication
    ){
        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        User admin = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Admin not found: " + authentication.getName()));
        User requester = request.getRequester();

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
        artworkRepository.save(artwork);

        request.setApprovedArtworkId(artwork.getId());
        request.setDecidedAt(LocalDateTime.now());

        notificationService.create(
                NotificationEnum.ART_REQUEST_STATUS,
                requester,
                admin,
                "Status of your art request has been changed",
                buildArtRequestStatusBody(request),
                artwork.getId() != null ? artwork.getId().toString() : null
        );


        acceptReviewsForPendingArt(request.getId(), request.getApprovedArtworkId());
        return newArtRequestRepository.save(request);
    }

    @Transactional
    public NewArtRequest rejectArtRequest(
            RejectDTO rejectReason,
            Long id,
            Authentication authentication
    ) {
        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        User admin = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Admin not found: " + authentication.getName()));
        User requester = request.getRequester();

        if(rejectReason != null){
            request.setRejectionReason(rejectReason.reason());
        }
        request.setDecidedAt(LocalDateTime.now());

        notificationService.create(
                NotificationEnum.ART_REQUEST_STATUS,
                requester,
                admin,
                "Status of your art request has changed",
                buildArtRequestStatusBody(request),
                null
        );
        return newArtRequestRepository.save(request);
    }

    public List<Review> acceptReviewsForPendingArt(
        Long artRequestId,
        Long acceptedArtId
    ){
        Artwork artwork = artworkRepository.findById(acceptedArtId)
                .orElseThrow(() -> new IllegalArgumentException("Artwork not found: " + acceptedArtId));
        List<Review> reviews = reviewRepository.findAllByArtRequestId(artRequestId);
        for (Review review : reviews) {
            review.setArtwork(artwork);
            review.setTitle(artwork.getName());
            review.setArtAuthor(artwork.getAuthor());
            review.setArtDescription(artwork.getDescription());
        }
        return reviews;
    }

    private String buildArtRequestStatusBody(NewArtRequest request) {
        String workName = request.getName() != null && !request.getName().isBlank()
                ? request.getName()
                : "Your work";

        return switch (request.getStatus()) {
            case CHANGED -> workName + " was approved with admin edits.";
            case ACCEPTED -> workName + " was approved.";
            case REJECTED -> workName + " was rejected.";
            case PENDING -> workName + " is pending moderation.";
        };
    }

//    @Transactional
//    public NewArtRequest changeRequest(
//            NewArtRequestDTO dto,
//            String adminNote,
//            Long id,
//            Authentication authentication
//    ){
//        NewArtRequest request = newArtRequestRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));
//
//        User admin = userRepository.findByUsernameIgnoreCase(authentication.getName())
//                .orElseThrow(() -> new IllegalStateException("Admin not found: " + authentication.getName()));
//        User requester = userRepository.findById(request.getRequester().getId())
//                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getRequester().getId()));
//
//        request.setStatus(ReviewStatusEnum.CHANGED);
//        request.setType(dto.getType());
//        request.setName(dto.getName());
//        request.setAuthor(dto.getAuthor());
//        request.setDescription(dto.getDescription());
//        request.setDecidedAt(LocalDateTime.now());
//        if(!adminNote.isBlank()){
//            request.setAdminNote(adminNote);
//        }
//        newArtRequestRepository.save(request);
//
//        notificationService.create(
//                NotificationEnum.ART_REQUEST_STATUS,
//                requester,
//                admin,
//                "Status of your art request has been changed",
//                request.getName() + " is now: " + request.getStatus() + "ed",
//                null
//        );
//
//        approveArtRequest(dto, id, authentication);
//
//        return request;
//    }
}
