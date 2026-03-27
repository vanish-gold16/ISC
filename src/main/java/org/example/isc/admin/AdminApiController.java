package org.example.isc.admin;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.example.isc.admin.service.AdminService;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.opuscore.dto.AdminArtAnswerDTO;
import org.example.isc.opuscore.dto.NewArtRequestDTO;
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
    public ResponseEntity<List<NewArtRequestDTO>> getArtRequests(
            @RequestParam(value = "query", required = false) String query,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        String normalizedQuery = normalize(query);
        List<NewArtRequest> requests = normalizedQuery == null
                ? List.of()
                : newArtRequestRepository.searchByResolvedName(normalizedQuery);

        return ResponseEntity.ok(requests.stream().map(this::toDTO).toList());
    }

    @GetMapping("/art-requests/{id}")
    public ResponseEntity<NewArtRequestDTO> getArtRequest(
            @PathVariable Long id,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        return ResponseEntity.ok(toDTO(request));
    }

    @PatchMapping("/art-requests/{id}")
    public ResponseEntity<NewArtRequestDTO> editRequest(
            @PathVariable  Long id,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        // не знаю

        request.setStatus(ReviewStatusEnum.CHANGED);
        return ResponseEntity.ok(toDTO(request));
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
            @Valid @RequestBody String rejectReason,
            @PathVariable Long id,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        NewArtRequest request = newArtRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + id));

        if(!(request.getStatus() == ReviewStatusEnum.REJECTED)){
            return ResponseEntity.badRequest().build();
        }
        else {
            request.setStatus(ReviewStatusEnum.REJECTED);
        }
        newArtRequestRepository.save(request);

        return ResponseEntity.ok(toAnswer(adminService.rejectArtRequest(rejectReason, id, authentication)));
    }

    @PostMapping("/admin/api/art-requests/{id}/request-changes")
    public ResponseEntity<AdminArtAnswerDTO> changeRequest(
            @Valid @RequestBody NewArtRequestDTO dto,
            @Valid @RequestBody String adminNote,
            @PathVariable Long id,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        return ResponseEntity.ok(toAnswer(adminService.changeRequest(dto, adminNote, id, authentication)));
    }

    private NewArtRequestDTO toDTO(NewArtRequest request){
        return new NewArtRequestDTO(
                request.getRequester().getId(),
                request.getType(),
                request.getName(),
                request.getAuthor(),
                request.getDescription(),
                request.getCoverUrl()
        );
    }

    private AdminArtAnswerDTO toAnswer(NewArtRequest request){
        return new AdminArtAnswerDTO(
                request.getRequester().getId(),
                request.getType(),
                request.getName(),
                request.getAuthor(),
                request.getDescription(),
                request.getCoverUrl(),
                request.getStatus(),
                LocalDateTime.now()
        );
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
