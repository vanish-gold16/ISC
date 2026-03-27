package org.example.isc.admin;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.example.isc.admin.service.AdminService;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.AdminArtAnswerDTO;
import org.example.isc.opuscore.dto.NewArtRequestDTO;
import org.example.isc.opuscore.dto.RejectDTO;
import org.example.isc.opuscore.enums.ArtTypeEnum;
import org.example.isc.opuscore.enums.ReviewStatusEnum;
import org.example.isc.opuscore.models.Artwork;
import org.example.isc.opuscore.models.NewArtRequest;
import org.example.isc.opuscore.repositories.NewArtRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/api")
public class AdminApiController {

    private final UserRepository userRepository;
    private final NewArtRequestRepository newArtRequestRepository;
    private final AdminService adminService;

    public AdminApiController(UserRepository userRepository, NewArtRequestRepository newArtRequestRepository, AdminService adminService) {
        this.userRepository = userRepository;
        this.newArtRequestRepository = newArtRequestRepository;
        this.adminService = adminService;
    }

    @GetMapping("/art-requests")
    public ResponseEntity<List<AdminArtAnswerDTO>> getArtRequests(
            @RequestParam(value = "query", required = false) String query,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        String normalizedQuery = normalize(query);
        List<NewArtRequest> requests = normalizedQuery == null
                ? newArtRequestRepository.findByStatusOrderByCreatedAtAsc(ReviewStatusEnum.PENDING)
                : newArtRequestRepository.searchByResolvedName(normalizedQuery);

        return ResponseEntity.ok(requests.stream().map(this::toAnswer).toList());
    }

    @GetMapping("/art-requests/{id}")
    public ResponseEntity<AdminArtAnswerDTO> getArtRequest(
            @PathVariable Long id,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        return ResponseEntity.ok(toAnswer(request));
    }

    @PatchMapping("/art-requests/{id}")
    public ResponseEntity<AdminArtAnswerDTO> editRequest(
            @PathVariable  Long id,
            @Valid @RequestBody AdminArtAnswerDTO answer,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        request.setType(answer.getType());
        request.setName(answer.getName());
        request.setAuthor(answer.getAuthor());
        request.setDescription(answer.getDescription());
        request.setCoverUrl(answer.getImageUrl());
        if(answer.getAdminNote() != null){
            request.setAdminNote(answer.getAdminNote());
        }
        request.setStatus(ReviewStatusEnum.CHANGED);
        request.setUpdatedAt(LocalDateTime.now());

        newArtRequestRepository.save(request);
        return ResponseEntity.ok(toAnswer(request));
    }

    @PostMapping("/art-requests/{id}/approve")
    public ResponseEntity<AdminArtAnswerDTO> approveRequest(
            @PathVariable Long id,
            @Valid @RequestBody NewArtRequestDTO dto,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        if(dto == null){
            return ResponseEntity.badRequest().build();
        }


        if(request.getStatus() == ReviewStatusEnum.ACCEPTED ||
                request.getStatus() == ReviewStatusEnum.REJECTED){
            return ResponseEntity.ok().build();
        } else if(request.getStatus() == ReviewStatusEnum.PENDING){
            request.setStatus(ReviewStatusEnum.ACCEPTED);
        }

        newArtRequestRepository.save(request);

        return ResponseEntity.ok(toAnswer(adminService.approveArtRequest(dto, id, authentication)));
    }

    @PostMapping("/art-requests/{id}/reject")
    public ResponseEntity<AdminArtAnswerDTO> rejectRequest(
            @Valid @RequestBody RejectDTO rejectReason,
            @PathVariable Long id,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        if(request.getStatus() == ReviewStatusEnum.REJECTED){
            return ResponseEntity.badRequest().build();
        }
        else {
            request.setStatus(ReviewStatusEnum.REJECTED);
        }
        newArtRequestRepository.save(request);

        return ResponseEntity.ok(toAnswer(adminService.rejectArtRequest(rejectReason, id, authentication)));
    }

//    @PostMapping("/admin/api/art-requests/{id}/request-changes")
//    public ResponseEntity<AdminArtAnswerDTO> changeRequest(
//            @Valid @RequestBody NewArtRequestDTO dto,
//            @Valid @RequestBody String adminNote,
//            @PathVariable Long id,
//            Authentication authentication
//    ){
//        requireCurrentUser(authentication);
//
//        return ResponseEntity.ok(toAnswer(adminService.changeRequest(dto, adminNote, id, authentication)));
//    }
//

    private AdminArtAnswerDTO toAnswer(NewArtRequest request){
        AdminArtAnswerDTO answer = new AdminArtAnswerDTO(
                request.getRequester().getId(),
                request.getType(),
                request.getName(),
                request.getAuthor(),
                request.getDescription(),
                request.getCoverUrl(),
                request.getStatus(),
                LocalDateTime.now()
        );
        answer.setId(request.getId());
        if(request.getAdminNote() != null){
            answer.setAdminNote(request.getAdminNote());
        }
        if(request.getRejectionReason() != null && request.getStatus() == ReviewStatusEnum.REJECTED){
            answer.setRejectionReason(request.getRejectionReason());
        }
        if((request.getStatus() == ReviewStatusEnum.ACCEPTED ||
           request.getStatus() == ReviewStatusEnum.CHANGED) &&
           request.getApprovedArtworkId() != null){
            answer.setApprovedArtworkId(request.getApprovedArtworkId());
        }
        answer.setCreatedAt(request.getCreatedAt());
        if(request.getStatus() == ReviewStatusEnum.CHANGED){
            answer.setUpdatedAt(request.getUpdatedAt());
        }
        return answer;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private User requireCurrentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

}
