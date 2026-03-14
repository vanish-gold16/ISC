package org.example.isc.main.secured.scholarhub.controller;

import org.example.isc.main.secured.models.scholarship.Schedule;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.repositories.scholarhub.SchedulesRepository;
import org.example.isc.main.secured.scholarhub.HubService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/scholar-hub")
public class HubController {

    private final HubService hubService;
    private final UserRepository userRepository;
    private final SchedulesRepository schedulesRepository;

    public HubController(HubService hubService, UserRepository userRepository, SchedulesRepository schedulesRepository) {
        this.hubService = hubService;
        this.userRepository = userRepository;
        this.schedulesRepository = schedulesRepository;
    }

    @GetMapping
    public String getHub(
            Authentication authentication,
            Model model
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        Schedule currentSchedule = schedulesRepository.findByUser(me);

        if (currentSchedule == null) {
            return "redirect:/scholar-hub/schedule/setup";
        }

        model.addAttribute("title", "ScholarHub");
        model.addAttribute("user", me);
        model.addAttribute("schedule", currentSchedule);

        return "/private/scholar-hub";
    }

    @GetMapping("/schedule/setup")
    public String getSetup(
            Authentication authentication,
            Model model
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        model.addAttribute("title", "Schedule setup");
        model.addAttribute("user", me);

        return "/private/scholar-hub-setup";
    }

}
