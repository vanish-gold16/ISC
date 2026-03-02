package org.example.isc.main.secured.messenger;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/messenger")
public class MessengerController {

    @GetMapping()
    public String messengerPage(
            Authentication authentication,
            Model model
    ){
        model.addAttribute("title", "Messenger");

        List<Map<String, Object>> conversations = List.of(
                Map.of(
                        "id", 1L,
                        "name", "Yuki Tanaka",
                        "avatar", "/images/private/profile/common-profile.png",
                        "lastMessage", "Let's meet at 6? I can bring the notes.",
                        "time", "2m",
                        "unread", 2,
                        "online", true,
                        "friend", true
                ),
                Map.of(
                        "id", 2L,
                        "name", "Campus Housing",
                        "avatar", "/images/private/profile/common-profile.png",
                        "lastMessage", "Your room key is ready for pickup.",
                        "time", "38m",
                        "unread", 0,
                        "online", false,
                        "friend", false
                ),
                Map.of(
                        "id", 3L,
                        "name", "Amina Idris",
                        "avatar", "/images/private/profile/common-profile.png",
                        "lastMessage", "Shared the syllabus in the drive.",
                        "time", "1h",
                        "unread", 0,
                        "online", true,
                        "friend", true
                ),
                Map.of(
                        "id", 4L,
                        "name", "ISC Events",
                        "avatar", "/images/private/profile/common-profile.png",
                        "lastMessage", "Reminder: Cultural night this Friday!",
                        "time", "Yesterday",
                        "unread", 1,
                        "online", false,
                        "friend", false
                )
        );

        Map<String, Object> activeConversation = Map.of(
                "id", 1L,
                "name", "Yuki Tanaka",
                "avatar", "/images/private/profile/common-profile.png",
                "subtitle", "Computer Science • Tokyo",
                "online", true,
                "friend", true
        );

        List<Map<String, Object>> messages = List.of(
                Map.of(
                        "fromMe", false,
                        "text", "Hey! Are we still on for the study meetup?",
                        "time", "18:21"
                ),
                Map.of(
                        "fromMe", true,
                        "text", "Yes! I reserved a table at the library cafe.",
                        "time", "18:22",
                        "status", "seen"
                ),
                Map.of(
                        "fromMe", false,
                        "text", "Great. Should we focus on chapter 6 first?",
                        "time", "18:23"
                ),
                Map.of(
                        "fromMe", true,
                        "text", "Yep. Also brought the cheat sheet.",
                        "time", "18:24",
                        "status", "delivered"
                ),
                Map.of(
                        "fromMe", false,
                        "text", "Let's meet at 6? I can bring the notes.",
                        "time", "18:25"
                )
        );

        List<Map<String, Object>> quickActions = List.of(
                Map.of("label", "Share file", "icon", "file"),
                Map.of("label", "Create poll", "icon", "poll"),
                Map.of("label", "Schedule", "icon", "calendar")
        );

        model.addAttribute("conversations", conversations);
        model.addAttribute("activeConversation", activeConversation);
        model.addAttribute("messages", messages);
        model.addAttribute("quickActions", quickActions);

        return "/private/messenger";
    }

    @GetMapping("/{id}")
    public String userPage(
            Authentication authentication,
            Model model
    ){


        return "/private/user-page";
    }

}
