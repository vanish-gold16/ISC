package org.example.isc.opuscore;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/opuscore")
public class MainController {

    @GetMapping
    public String getMain(Authentication authentication){


        return "/opuscore/main";
    }

}
