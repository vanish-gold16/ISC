package org.example.isc.main.secured.scholarhub.controller.api;

import jakarta.validation.Valid;
import org.example.isc.main.dto.scholarship.HomeworkDTO;
import org.example.isc.main.secured.models.scholarship.Grade;
import org.example.isc.main.secured.models.scholarship.Homework;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.GradeRepository;
import org.example.isc.main.secured.repositories.scholarhub.HomeworkRepository;
import org.example.isc.main.secured.scholarhub.service.HomeworkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/scholar-hub/homework")
public class HomeworkApiController {

    private static final Logger log = LoggerFactory.getLogger(HomeworkApiController.class);
    private final UserRepository userRepository;
    private final HomeworkRepository homeworkRepository;
    private final GradeRepository gradeRepository;
    private final HomeworkService homeworkService;

    public HomeworkApiController(UserRepository userRepository, HomeworkRepository homeworkRepository, GradeRepository gradeRepository, HomeworkService homeworkService) {
        this.userRepository = userRepository;
        this.homeworkRepository = homeworkRepository;
        this.gradeRepository = gradeRepository;
        this.homeworkService = homeworkService;
    }

    @GetMapping
    public ResponseEntity<List<HomeworkDTO>> getHomeWorkByWeek(
            @RequestParam("weekStart") LocalDate weekStart,
            @RequestParam(value = "dueDaySubjectId", required = false) Long dueDaySubjectId,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        List<Homework> homeworks;
        if (dueDaySubjectId == null) {
            homeworks = homeworkRepository.findAllByWeekStart(weekStart);
        } else {
            homeworks = homeworkRepository.findAllByWeekStartAndDueDaySubjectId(weekStart, dueDaySubjectId);
        }

        return ResponseEntity.ok(homeworks.stream().map(this::toHomeworkDTO).toList());
    }

    @PostMapping
    public ResponseEntity<Void> postHomework(
            @Valid @RequestBody HomeworkDTO homeworkDTO,
            Authentication authentication
    ){
        requireCurrentUser(authentication);
        Homework savedHomework = homeworkService.create(homeworkDTO);
        log.info("Homework created: {}", savedHomework.getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> editHomework(
        @PathVariable Long id,
        @Valid @RequestBody HomeworkDTO homeworkDTO,
        Authentication authentication
    ){
        requireCurrentUser(authentication);

        if(id.equals(homeworkDTO.getId())) return ResponseEntity.badRequest().build();

        Homework editedHomework = homeworkService.edit(id, homeworkDTO);
        log.info("Homework edited: " +  homeworkDTO.getId());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHomework(
            @PathVariable Long id,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        homeworkService.delete(id);
        log.info("Homework deleted: " +  id);
        return ResponseEntity.ok().build();
    }

    private HomeworkDTO toHomeworkDTO(Homework homework){
        Long dueDaySubjectId = homework.getDueDaySubjectId() != null ? homework.getDueDaySubjectId() : null;
        Grade grade = homework.getGradeId() != null
                ? gradeRepository.findById(homework.getGradeId()).orElse(null)
                : null;
        return new HomeworkDTO(
                homework.getId(),
                homework.getTitle(),
                homework.getDetails(),
                homework.getPriority(),
                homework.getSubjectId(),
                dueDaySubjectId,
                homework.getStatus(),
                homework.getWeekStart(),
                homework.getGradeId(),
                grade != null ? grade.getGradingSystem() : null,
                grade != null ? grade.getValue() : null
        );
    }

    private User requireCurrentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

}
