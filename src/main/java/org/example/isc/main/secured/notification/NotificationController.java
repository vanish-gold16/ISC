package org.example.isc.main.secured.notification;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NotificationController {

    @GetMapping("/notifications")
    public String notifications(
            Model model
    ) {
        model.addAttribute("title", "Notifications");

        return "/private/notifications";
    }

}
