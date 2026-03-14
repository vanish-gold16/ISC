package org.example.isc.main.secured.scholarhub.controller;

import org.example.isc.main.dto.scholarship.NewSubjectDTO;
import org.example.isc.main.dto.scholarship.SubjectOptionDTO;
import org.example.isc.main.secured.models.scholarship.Subject;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.SubjectsRepository;
import org.example.isc.main.secured.repositories.scholarhub.TeachersRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
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
        Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        List<SubjectOptionDTO> subjects = subjectsRepository.findByUser(me);

        return ResponseEntity.ok(subjects);
    }

    @PostMapping
    public ResponseEntity<SubjectOptionDTO> getOrCreateSubject(
            Authentication authentication,
            NewSubjectDTO form
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        if(subjectsRepository.existsByUserAndFullName(me, form.getFullName())){
            SubjectOptionDTO option = subjectsRepository.findByUserAndFullNameIgnoreCase(me, form.getFullName());
            return ResponseEntity.ok(option);
        }
        Subject subject = new Subject(
                form.getShortName(),
                form.getFullName(),
                me,
                teachersRepository.findAllById(form.getTeacherId())
        );

        return ResponseEntity.ok(
                new SubjectOptionDTO(subject.getId(), subject.getFullName())
        );
    }

}
