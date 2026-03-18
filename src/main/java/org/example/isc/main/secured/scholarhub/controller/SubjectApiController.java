package org.example.isc.main.secured.scholarhub.controller;

import jakarta.validation.Valid;
import org.example.isc.main.dto.scholarship.NewSubjectDTO;
import org.example.isc.main.dto.scholarship.SubjectOptionDTO;
import org.example.isc.main.secured.models.scholarship.Subject;
import org.example.isc.main.secured.models.scholarship.Teacher;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/scholar-hub/subjects")
public class SubjectApiController {

    private final UserRepository userRepository;
    private final SubjectsRepository subjectsRepository;
    private final TeachersRepository teachersRepository;

    public SubjectApiController(UserRepository userRepository, SubjectsRepository subjectsRepository, TeachersRepository teachersRepository) {
        this.userRepository = userRepository;
        this.subjectsRepository = subjectsRepository;
        this.teachersRepository = teachersRepository;
    }

    @GetMapping
    public ResponseEntity<List<SubjectOptionDTO>> getSubjectResult(
            @RequestParam(value = "query", required = false) String query,
            Authentication authentication
    ) {
        User me = requireCurrentUser(authentication);

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
    ) {
        User me = requireCurrentUser(authentication);
        Subject subject = findOrCreateSubject(me, form);
        return ResponseEntity.ok(toOption(subjectsRepository.save(subject)));
    }

    @GetMapping("/edit")
    public ResponseEntity<SubjectOptionDTO> editSubject(
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "query", required = false) String query,
            Authentication authentication
    ) {
        User me = requireCurrentUser(authentication);

        Subject subject;
        if (id != null) {
            subject = subjectsRepository.findByIdAndUser(id, me)
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + id));
        } else {
            String normalized = normalize(query);
            subject = subjectsRepository.findByUserAndResolvedNameIgnoreCase(me, normalized)
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + normalized));
        }
        return ResponseEntity.ok(toOption(subject));
    }

    @PostMapping("/edit")
    public ResponseEntity<SubjectOptionDTO> saveEdited(
            @Valid @RequestBody NewSubjectDTO form,
            Authentication authentication
    ) {
        User me = requireCurrentUser(authentication);
        String fullName = normalize(form.getFullName());
        if (fullName == null) {
            throw new IllegalArgumentException("Subject full name is required");
        }

        Subject subject = form.getId() == null
                ? subjectsRepository.findByUserAndFullNameIgnoreCase(me, fullName)
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + fullName))
                : subjectsRepository.findByIdAndUser(form.getId(), me)
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + form.getId()));
        applySubjectFields(subject, form, fullName);

        return ResponseEntity.ok(toOption(subjectsRepository.save(subject)));
    }

    private Subject findOrCreateSubject(User user, NewSubjectDTO form) {
        String fullName = normalize(form.getFullName());
        if (fullName == null) {
            throw new IllegalArgumentException("Subject full name is required");
        }

        Subject subject = subjectsRepository.findByUserAndResolvedNameIgnoreCase(user, fullName)
                .orElseGet(() -> {
                    Subject newSubject = new Subject();
                    newSubject.setUser(user);
                    newSubject.setTeachers(new ArrayList<>());
                    return newSubject;
                });

        applySubjectFields(subject, form, fullName);
        return subject;
    }

    private void applySubjectFields(Subject subject, NewSubjectDTO form, String fullName) {
        subject.setFullName(fullName);

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
            subject.setTeachers(List.of(teacher));
        }
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

    private String normalizeColor(String color) {
        String normalized = normalize(color);
        Pattern pattern = Pattern.compile("^#[0-9A-Fa-f]{6}$");
        return normalized != null && pattern.matcher(normalized).matches() ? normalized : null;
    }

    private String buildShortName(String fullName) {
        String trimmed = fullName.trim();
        String[] words = trimmed.split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String word : words) {
            if (!word.isBlank() && Character.isLetterOrDigit(word.charAt(0))) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }

        if (!initials.isEmpty()) {
            return initials.length() <= 12 ? initials.toString() : initials.substring(0, 12);
        }

        return trimmed.length() <= 12 ? trimmed : trimmed.substring(0, 12);
    }

    private User requireCurrentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

}
