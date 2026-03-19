package org.example.isc.main.secured.scholarhub.controller.api;

import jakarta.validation.Valid;
import org.example.isc.main.dto.scholarship.HomeworkDTO;
import org.example.isc.main.secured.models.scholarship.Homework;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.DaySubjectRepository;
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
    private final DaySubjectRepository daySubjectRepository;
    private final HomeworkService homeworkService;

    public HomeworkApiController(UserRepository userRepository, HomeworkRepository homeworkRepository, DaySubjectRepository daySubjectRepository, HomeworkService homeworkService) {
        this.userRepository = userRepository;
        this.homeworkRepository = homeworkRepository;
        this.daySubjectRepository = daySubjectRepository;
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
        Homework savedHomework = homeworkRepository.save(toHomework(homeworkDTO));
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

        homeworkService.edit(id, homeworkDTO);
        log.info("Homework edited: " +  homeworkDTO.getId());

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHomework(
            @PathVariable Long id,
            Authentication authentication
    ){
        requireCurrentUser(authentication);

        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Homework not found: " + id));

        homeworkRepository.delete(homework);
        log.info("Homework deleted: " +  homework.getId());
        return ResponseEntity.ok().build();
    }

    private HomeworkDTO toHomeworkDTO(Homework homework){
        Long dueDaySubjectId = homework.getDueDaySubjectId() != null ? homework.getDueDaySubjectId() : null;
        return new HomeworkDTO(
                homework.getId(),
                homework.getTitle(),
                homework.getDetails(),
                homework.getPriority(),
                homework.getSubjectId(),
                dueDaySubjectId,
                homework.getStatus(),
                homework.getWeekStart()
        );
    }

    private Homework toHomework(HomeworkDTO homeworkDTO){
        var lesson = daySubjectRepository.findById(homeworkDTO.getDueDaySubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + homeworkDTO.getDueDaySubjectId()));
        Homework homework = new Homework();
        homework.setTitle(homeworkDTO.getTitle());
        homework.setDetails(homeworkDTO.getDetails());
        homework.setPriority(homeworkDTO.getPriority());
        homework.setSubjectId(lesson.getSubject().getId());
        homework.setStatus(homeworkDTO.getStatus());
        homework.setDueDaySubjectId(lesson.getId());
        homework.setWeekStart(homeworkDTO.getWeekStart());
        return  homework;
    }

    private User requireCurrentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

}
