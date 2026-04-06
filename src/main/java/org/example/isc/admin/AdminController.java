package org.example.isc.admin;

import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping({"", "/", "/management"})
    public String getManagement(
            Authentication authentication,
            Model model
    ){
        populateAdminModel(authentication, model, "Admin panel", "dashboard");
        return "private/admin/management";
    }

    @GetMapping("/new-reviews")
    public String getNewReviews(Authentication authentication, Model model) {
        populateAdminModel(authentication, model, "Admin panel | New reviews", "new-reviews");
        return "private/admin/management";
    }

    @GetMapping("/new-arts")
    public String getNewArts(Authentication authentication, Model model) {
        populateAdminModel(authentication, model, "Admin panel | New arts", "new-arts");
        return "private/admin/management";
    }

    @GetMapping("/edit-art")
    public String editArt(Authentication authentication, Model model){
        populateAdminModel(authentication, model, "Admin panel | Edit art", "edit-art");
        return "private/admin/management";
    }

    private void populateAdminModel(Authentication authentication, Model model, String title, String adminSection) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        model.addAttribute("title", title);
        model.addAttribute("user", me);
        model.addAttribute("adminSection", adminSection);
    }

}
