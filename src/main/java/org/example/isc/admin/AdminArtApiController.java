package org.example.isc.admin;

import jakarta.validation.Valid;
import org.example.isc.admin.service.AdminService;
import org.example.isc.cloudinary.ImageService;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.AdminArtAnswerDTO;
import org.example.isc.opuscore.dto.NewArtRequestDTO;
import org.example.isc.opuscore.dto.RejectDTO;
import org.example.isc.opuscore.enums.ReviewStatusEnum;
import org.example.isc.opuscore.models.NewArtRequest;
import org.example.isc.opuscore.repositories.NewArtRequestRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/admin/api")
public class AdminArtApiController {

    private final UserRepository userRepository;
    private final NewArtRequestRepository newArtRequestRepository;
    private final AdminService adminService;
    private final ImageService imageService;

    public AdminArtApiController(UserRepository userRepository, NewArtRequestRepository newArtRequestRepository, AdminService adminService, ImageService imageService) {
        this.userRepository = userRepository;
        this.newArtRequestRepository = newArtRequestRepository;
        this.adminService = adminService;
        this.imageService = imageService;
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

    @PostMapping("/art-requests/cover")
    public ResponseEntity<String> uploadArtRequestCover(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        User me = requireCurrentUser(authentication);
        return ResponseEntity.ok(imageService.uploadReviewImage(file, me.getId()));
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
        request.setDescription(blankToNull(answer.getDescription()));
        request.setCoverUrl(blankToNull(answer.getImageUrl()));
        request.setAdminNote(blankToNull(answer.getAdminNote()));
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

        boolean edited = isRequestEdited(request, dto);
        request.setType(dto.getType());
        request.setName(dto.getName());
        request.setAuthor(dto.getAuthor());
        request.setDescription(blankToNull(dto.getDescription()));
        request.setCoverUrl(blankToNull(dto.getImageUrl()));

        if(request.getStatus() == ReviewStatusEnum.ACCEPTED ||
                request.getStatus() == ReviewStatusEnum.REJECTED){
            return ResponseEntity.ok().build();
        } else if(request.getStatus() == ReviewStatusEnum.PENDING){
            request.setStatus(edited ? ReviewStatusEnum.CHANGED : ReviewStatusEnum.ACCEPTED);
        }

        if (edited) {
            request.setUpdatedAt(LocalDateTime.now());
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

        if(!(request.getStatus() == ReviewStatusEnum.PENDING)){
            return ResponseEntity.badRequest().build();
        }
        else {
            return ResponseEntity.ok(toAnswer(adminService.rejectArtRequest(rejectReason, id, authentication)));
        }
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
        answer.setRequesterUsername(request.getRequester().getUsername());
        return answer;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private boolean isRequestEdited(NewArtRequest request, NewArtRequestDTO dto) {
        return !Objects.equals(request.getType(), dto.getType())
                || !Objects.equals(blankToNull(request.getName()), blankToNull(dto.getName()))
                || !Objects.equals(blankToNull(request.getAuthor()), blankToNull(dto.getAuthor()))
                || !Objects.equals(blankToNull(request.getDescription()), blankToNull(dto.getDescription()))
                || !Objects.equals(blankToNull(request.getCoverUrl()), blankToNull(dto.getImageUrl()));
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private User requireCurrentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

}
