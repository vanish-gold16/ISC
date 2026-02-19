package org.example.isc.main.secured.home;

import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.example.isc.main.secured.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home")
public class HomeController {

    private final HomeService homeService;
    private final UserRepository userRepository;

    public HomeController(HomeService homeService, UserRepository userRepository) {
        this.homeService = homeService;
        this.userRepository = userRepository;
    }

    @GetMapping()
    public String getHome(Model model, Authentication authentication){
        String username = authentication.getName();
        User me = userRepository.findByUsernameIgnoreCase(username).orElseThrow(() ->
                new IllegalStateException("Logged-in user not found: " + username));
        model.addAttribute("title", "Home");
        model.addAttribute("posts", homeService.getFeed(me.getId()));

        return "private/home";
    }

}
