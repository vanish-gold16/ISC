package org.example.isc.main.secured.notification;

import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NotificationController {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public NotificationController(UserRepository userRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public String notifications(
            Model model,
            Authentication authentication
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                        .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        model.addAttribute("title", "Notifications");
        model.addAttribute("notifications", notificationService.list(me, 20));
        model.addAttribute("unreadCount", notificationService.unreadCount(me));

        return "/private/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String read(
            @PathVariable Long id,
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                        .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        notificationService.markRead(id, me);

        return "redirect:/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String readAll(
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        notificationService.markAllRead(me);

        return "redirect:/notifications";
    }

}
