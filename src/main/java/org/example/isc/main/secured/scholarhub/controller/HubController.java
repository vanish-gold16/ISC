package org.example.isc.main.secured.scholarhub.controller;

import org.example.isc.main.dto.messenger.ScheduleView;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.main.secured.scholarhub.HubService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/scholar-hub")
public class HubController {

    private final HubService hubService;
    private final UserRepository userRepository;

    public HubController(HubService hubService, UserRepository userRepository) {
        this.hubService = hubService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String getHub(
            Authentication authentication,
            Model model
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        ScheduleView currentSchedule = hubService.getScheduleForHub(authentication, 8);
        if (currentSchedule == null) return "redirect:/scholar-hub/schedule/setup";

        model.addAttribute("title", "ScholarHub");
        model.addAttribute("user", me);
        model.addAttribute("schedule", currentSchedule);

        return "/private/scholar-hub";
    }

    @GetMapping("/schedule")
    public String getSchedule(
            Authentication authentication,
            Model model
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        ScheduleView currentSchedule = hubService.getScheduleForHub(authentication, 0);
        if (currentSchedule == null) return "redirect:/scholar-hub/schedule/setup";

        model.addAttribute("title", "ScholarHub schedule");
        model.addAttribute("user", me);
        model.addAttribute("schedule", currentSchedule);

        return "/private/scholar-hub-schedule";
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

    @PostMapping("/schedule/setup")
    private String postSetup(
            @RequestParam("schedulePayload") String schedulePayload,
            Authentication authentication
    ){
        hubService.setup(schedulePayload, authentication);
        return "redirect:/scholar-hub";
    }

}
