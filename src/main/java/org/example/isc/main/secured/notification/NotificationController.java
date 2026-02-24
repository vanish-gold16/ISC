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

    // Что тебе дописать на бэке под это:
    //
    //  1. В @ControllerAdvice прокидывать в модель на все приватные страницы:
    //
    //  - unreadNotifications: long
    //  - notificationsPreview: List<Notification> (уже отсортированный DESC, лимит 5)
    //
    //  2. На страницу /notifications прокидывать:
    //
    //  - notifications (полный/пагинированный список)
    //  - unreadCount
    //
    //  3. Нужные ручки (если еще нет):
    //
    //  - GET /notifications (большой список)
    //  - POST /notifications/{id}/read
    //  - POST /notifications/read-all
    //
    //  4. Обязательно сохранить уведомления в NotificationService#create(...):
    //
    //  - сейчас объект создается, но нужен notificationsRepository.save(notification).
    //
    //  Если хочешь, следующим шагом дам тебе готовый код для GlobalModelAttributes (чтобы дропдаун работал на всех страницах сразу).
    //
    //   - В NotificationController.java (C:\Users\mitro\IdeaProjects\ISC\src\main\java\org\example\isc\main\secured\notification\NotificationController.java) у POST /notifications/read-
    //    all стоит лишний @PathVariable Long id (его нет в URL). Это лучше убрать, иначе эта ручка будет падать.

}
