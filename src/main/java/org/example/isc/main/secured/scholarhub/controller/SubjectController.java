package org.example.isc.main.secured.scholarhub.controller;

import org.example.isc.main.secured.models.scholarship.Subject;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.SubjectsRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/scholar-hub/subject")
public class SubjectController {

    private final UserRepository userRepository;
    private final SubjectsRepository subjectsRepository;

    public SubjectController(UserRepository userRepository, SubjectsRepository subjectsRepository) {
        this.userRepository = userRepository;
        this.subjectsRepository = subjectsRepository;
    }

    @GetMapping("/{id}")
    public String getSubject(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ){
        requireCurrentUser(authentication);
        Subject currentSubject = subjectsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + id));

        if(currentSubject.getUser().getUsername().equals(authentication.getName())){
            model.addAttribute("subject", currentSubject);
        }
        model.addAttribute("user",  currentSubject.getUser());

        return "/private/scholar-hub/subject";
    }

    private User requireCurrentUser(Authentication authentication) {
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
    }

}
