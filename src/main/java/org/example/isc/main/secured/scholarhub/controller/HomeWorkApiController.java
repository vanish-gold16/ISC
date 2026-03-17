package org.example.isc.main.secured.scholarhub.controller;

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

    // не понимаю
    @GetMapping
    public ResponseEntity<List<HomeworkDTO>> getHomeWorkByWeek(
            @RequestParam(value = "query", required = false) String query,
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        String normalizedQuery = normalize(query);
        LocalDate parsedDate = LocalDate.parse(normalizedQuery);
        List<Homework> homeworks = homeworkRepository.findAllByWeekStartAndSubjectId();

        return ResponseEntity.ok(homeworks.stream().map(this::toHomeworkDTO).toList());
    }

    @PostMapping
    public ResponseEntity<Void> postHomework(
        @RequestParam("form") HomeworkDTO homeworkDTO
    ){
        Homework homework = toHomework(homeworkDTO);
        homeworkRepository.save(homework);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> editHomework(
        @PathVariable Long id,
        @RequestParam("form") HomeworkDTO homeworkDTO
    ){
        homeworkService.edit(id, homeworkDTO);

        return ResponseEntity.ok().build();
    }



    private HomeworkDTO toHomeworkDTO(Homework homework){
        return new HomeworkDTO(
                homework.getTitle(),
                homework.getDetails(),
                homework.getPriority(),
                homework.getSubject().getId(),
                homework.getStatus(),
                homework.getWeekStart()
        );
    }

    private Homework toHomework(HomeworkDTO homeworkDTO){
        DaySubject lesson = daySubjectRepository.findById(homeworkDTO.getDaySubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + homeworkDTO.getDaySubjectId()));
        return new Homework(
                homeworkDTO.getTitle(),
                homeworkDTO.getDetails(),
                homeworkDTO.getPriority(),
                lesson.getSubject(),
                homeworkDTO.getStatus(),
                homeworkDTO.getWeekStart()
                );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }
}
