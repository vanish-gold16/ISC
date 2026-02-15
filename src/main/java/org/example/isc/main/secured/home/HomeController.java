package org.example.isc.main.secured.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/")
    public String home(Model model){
        Long userId = 1L; // hardcode
        model.addAttribute("title", "Home");
        model.addAttribute("posts", homeService.getFeed(userId));

        return "home";
    }

}
