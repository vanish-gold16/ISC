package org.example.isc.main.secured.scholarhub.controller.api;

import jakarta.validation.Valid;
import org.example.isc.main.dto.scholarship.GradeDTO;
import org.example.isc.main.secured.models.scholarship.Grade;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.scholarhub.service.GradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/scholar-hub/grades")
public class GradeApiController {

    private final UserRepository userRepository;
    private final GradeService gradeService;

    public GradeApiController(UserRepository userRepository, GradeService gradeService) {
        this.userRepository = userRepository;
        this.gradeService = gradeService;
    }

    @GetMapping
    public ResponseEntity<List<GradeDTO>> getAllGrades(
            Authentication authentication
    ){
        User me = requireCurrentUser(authentication);
        List<Grade> grades = gradeService.getAllForUser(me.getUsername());

        return ResponseEntity.ok(grades.stream().map(this::toDTO).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeDTO> getGrade(
            @PathVariable Long id,
            Authentication authentication
    ){
        User me = requireCurrentUser(authentication);
        Grade grade = gradeService.getForUser(id, me.getUsername());
        return ResponseEntity.ok(toDTO(grade));
    }

    @PostMapping
    public ResponseEntity<Void> createGrade(
            @Valid @RequestBody GradeDTO gradeDTO,
            Authentication authentication
    ){
        User me = requireCurrentUser(authentication);
        gradeService.create(gradeDTO, me.getUsername());
        return ResponseEntity.ok().build();
    }

    public GradeDTO toDTO(Grade grade) {
        return new GradeDTO(
                grade.getSubject().getId(),
                grade.getAssignedDaySubject() != null ? grade.getAssignedDaySubject().getId() : null,
                grade.getGradingSystem(),
                grade.getReason(),
                grade.getDescription(),
                grade.getValue(),
                grade.getConverted()
        );
    }

    private User requireCurrentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

}
