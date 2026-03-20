package org.example.isc.main.secured.scholarhub.controller.api;

import jakarta.validation.Valid;
import org.example.isc.main.dto.scholarship.GradeDTO;
import org.example.isc.main.secured.models.scholarship.ConvertGrade;
import org.example.isc.main.secured.models.scholarship.DaySubject;
import org.example.isc.main.secured.models.scholarship.Grade;
import org.example.isc.main.secured.models.scholarship.Subject;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.DaySubjectRepository;
import org.example.isc.main.secured.repositories.scholarhub.GradeRepository;
import org.example.isc.main.secured.repositories.scholarhub.SubjectsRepository;
import org.example.isc.main.secured.scholarhub.service.GradeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/scholar-hub/grades")
public class GradeApiController {

    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    private final ConvertGrade convertGrade = new ConvertGrade();
    private final SubjectsRepository subjectsRepository;
    private final DaySubjectRepository daySubjectRepository;
    private final GradeService gradeService;

    public GradeApiController(UserRepository userRepository, GradeRepository gradeRepository, SubjectsRepository subjectsRepository, DaySubjectRepository daySubjectRepository, GradeService gradeService, GradeService gradeService1) {
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
        this.subjectsRepository = subjectsRepository;
        this.daySubjectRepository = daySubjectRepository;
        this.gradeService = gradeService1;
    }

    @GetMapping
    public ResponseEntity<List<GradeDTO>> getAllGrades(
            Authentication authentication
    ){
        User me = requireCurrentUser(authentication);

        List<Grade> grades = new ArrayList<>();
        List<Subject> subjects = subjectsRepository.findAllByUserUsername(me.getUsername());
        if(subjects.isEmpty()) return ResponseEntity.noContent().build();

        for (Subject subject : subjects) {
            List<Grade> gradesForSubject = gradeRepository.findAllBySubjectIdAndSubjectUserUsername(
                    subject.getId(), me.getUsername()
            );
            grades.addAll(gradesForSubject);
        }

        return ResponseEntity.ok(grades.stream().map(this::toDTO).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradeDTO> getGrade(
            @PathVariable Long id,
            Authentication authentication
    ){
        requireCurrentUser(authentication);
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found: " + id));

        return ResponseEntity.ok(toDTO(grade));
    }

    @PostMapping
    public ResponseEntity<Void> createGrade(
            @Valid @RequestBody GradeDTO gradeDTO,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        if(gradeDTO == null) return ResponseEntity.badRequest().build();

        gradeRepository.save(gradeService.create(gradeDTO));
        return ResponseEntity.ok().build();
    }

    public GradeDTO toDTO(Grade grade) {
        return new GradeDTO(
                grade.getSubject().getId(),
                grade.getAssignedDaySubject().getId(),
                grade.getGradingSystem(),
                grade.getValue(),
                grade.getConverted()
        );
    }

    private User requireCurrentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

}
