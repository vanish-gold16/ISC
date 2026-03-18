package org.example.isc.main.secured.scholarhub.controller;

import jakarta.validation.Valid;
import org.example.isc.main.dto.scholarship.HomeworkDTO;
import org.example.isc.main.secured.models.scholarship.DaySubject;
import org.example.isc.main.secured.models.scholarship.Homework;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.DaySubjectRepository;
import org.example.isc.main.secured.repositories.scholarhub.HomeworkRepository;
import org.example.isc.main.secured.scholarhub.HomeworkService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/scholar-hub/homework")
public class HomeWorkApiController {

    private final UserRepository userRepository;
    private final HomeworkRepository homeworkRepository;
    private final DaySubjectRepository daySubjectRepository;
    private final HomeworkService homeworkService;

    public HomeWorkApiController(UserRepository userRepository, HomeworkRepository homeworkRepository, DaySubjectRepository daySubjectRepository, HomeworkService homeworkService) {
        this.userRepository = userRepository;
        this.homeworkRepository = homeworkRepository;
        this.daySubjectRepository = daySubjectRepository;
        this.homeworkService = homeworkService;
    }

    @GetMapping
    public ResponseEntity<List<HomeworkDTO>> getHomeWorkByWeek(
            @RequestParam("weekStart") LocalDate weekStart,
            @RequestParam(value = "daySubjectId", required = false) Long daySubjectId,
            Authentication authentication
    ){
        requireCurrentUser(authentication);
        List<Homework> homeworks = daySubjectId == null
                ? homeworkRepository.findAllByWeekStart(weekStart)
                : homeworkRepository.findAllByWeekStartAndDaySubject(weekStart, getLesson(daySubjectId));

        return ResponseEntity.ok(homeworks.stream().map(this::toHomeworkDTO).toList());
    }

    @PostMapping
    public ResponseEntity<Void> postHomework(
            @Valid @RequestBody HomeworkDTO homeworkDTO,
            Authentication authentication
    ){
        requireCurrentUser(authentication);
        homeworkRepository.save(toHomework(homeworkDTO));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> editHomework(
        @PathVariable Long id,
        @Valid @RequestBody HomeworkDTO homeworkDTO,
        Authentication authentication
    ){
        requireCurrentUser(authentication);

        homeworkService.edit(id, homeworkDTO);

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
        return ResponseEntity.ok().build();
    }

    private HomeworkDTO toHomeworkDTO(Homework homework){
        Long daySubjectId = homework.getDaySubject() != null ? homework.getDaySubject().getId() : null;
        return new HomeworkDTO(
                homework.getId(),
                homework.getTitle(),
                homework.getDetails(),
                homework.getPriority(),
                daySubjectId,
                homework.getStatus(),
                homework.getWeekStart()
        );
    }

    private Homework toHomework(HomeworkDTO homeworkDTO){
        DaySubject lesson = daySubjectRepository.findById(homeworkDTO.getDaySubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + homeworkDTO.getDaySubjectId()));
        Homework homework = new Homework();
        homework.setTitle(homeworkDTO.getTitle());
        homework.setDetails(homeworkDTO.getDetails());
        homework.setPriority(homeworkDTO.getPriority());
        homework.setSubject(lesson.getSubject());
        homework.setStatus(homeworkDTO.getStatus());
        homework.setDaySubject(lesson);
        homework.setWeekStart(homeworkDTO.getWeekStart());
        return  homework;
    }

    private DaySubject getLesson(Long daySubjectId) {
        return daySubjectRepository.findById(daySubjectId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + daySubjectId));
    }

    private User requireCurrentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

}
