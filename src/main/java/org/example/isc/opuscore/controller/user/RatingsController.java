package org.example.isc.opuscore.controller.user;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/opuscore/ratings")
public class RatingsController {

    @GetMapping
    public String getMyRatings(
            Authentication authentication,
            Model model
    ){
        return "/opuscore/my-ratings";
    }

}
