package org.example.isc.main.secured.scholarhub.controller;

import jakarta.validation.Valid;
import org.example.isc.main.dto.scholarship.HomeworkDTO;
import org.example.isc.main.dto.scholarship.NewSubjectDTO;
import org.example.isc.main.dto.scholarship.SubjectOptionDTO;
import org.example.isc.main.secured.models.scholarship.Homework;
import org.example.isc.main.secured.models.scholarship.Subject;
import org.example.isc.main.secured.models.scholarship.Teacher;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.HomeworkRepository;
import org.example.isc.main.secured.repositories.scholarhub.SubjectsRepository;
import org.example.isc.main.secured.repositories.scholarhub.TeachersRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RestController()
@RequestMapping("/scholar-hub/subjects")
public class SubjectApiController {

    private final UserRepository userRepository;
    private final SubjectsRepository subjectsRepository;
    private final TeachersRepository teachersRepository;
    private final HomeworkRepository homeworkRepository;

    public SubjectApiController(UserRepository userRepository, SubjectsRepository subjectsRepository, TeachersRepository teachersRepository, HomeworkRepository homeworkRepository) {
        this.userRepository = userRepository;
        this.subjectsRepository = subjectsRepository;
        this.teachersRepository = teachersRepository;
        this.homeworkRepository = homeworkRepository;
    }

    @GetMapping
    public ResponseEntity<List<SubjectOptionDTO>> getSubjectResult(
        @RequestParam(value = "query", required = false) String query,
        Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        String normalizedQuery = normalize(query);
        List<Subject> subjects = normalizedQuery == null
                ? List.of()
                : subjectsRepository.searchByUserAndResolvedName(me, normalizedQuery);

        return ResponseEntity.ok(subjects.stream().map(this::toOption).toList());
    }

    @PostMapping
    public ResponseEntity<SubjectOptionDTO> getOrCreateSubject(
            Authentication authentication,
            @Valid @RequestBody NewSubjectDTO form
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        String fullName = normalize(form.getFullName());
        if (fullName == null) {
            throw new IllegalArgumentException("Subject full name is required");
        }

        Subject subject = subjectsRepository.findByUserAndResolvedNameIgnoreCase(me, fullName)
                .orElseGet(() -> {
                    Subject newSubject = new Subject();
                    newSubject.setUser(me);
                    newSubject.setFullName(fullName);
                    newSubject.setTeachers(new ArrayList<>());
                    return newSubject;
                });

        String shortName = normalize(form.getShortName());
        subject.setShortName(shortName != null ? shortName : buildShortName(fullName));
        subject.setRoom(normalize(form.getRoom()));
        String color = normalizeColor(form.getColor());
        if (color != null) {
            subject.setColor(color);
        }

        String teacherName = normalize(form.getTeacherName());
        if (teacherName != null) {
            Teacher teacher = teachersRepository.findByFullNameIgnoreCase(teacherName)
                    .orElseGet(() -> {
                        Teacher newTeacher = new Teacher();
                        newTeacher.setFullName(teacherName);
                        newTeacher.setSubjects(new ArrayList<>());
                        return teachersRepository.save(newTeacher);
                    });
            List<Teacher> teachers = new ArrayList<>();
            teachers.add(teacher);
            subject.setTeachers(teachers);
        }

        Subject saved = subjectsRepository.save(subject);
        return ResponseEntity.ok(toOption(saved));
    }

    private SubjectOptionDTO toOption(Subject subject) {
        String teacherName = subject.getTeachers() != null && !subject.getTeachers().isEmpty()
                ? subject.getTeachers().get(0).getFullName()
                : null;
        return new SubjectOptionDTO(
                subject.getId(),
                subject.getFullName(),
                subject.getShortName(),
                teacherName,
                subject.getRoom(),
                subject.getColor()
        );
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeColor(String color){
        String normalized = normalize(color);
        Pattern pattern = Pattern.compile("^#[0-9A-Fa-f]{6}$");
        return normalized != null && pattern.matcher(normalized).matches() ? normalized : null;
    }

    private String buildShortName(String fullName) {
        return fullName.length() <= 12 ? fullName : fullName.substring(0, 12);
    }

}
