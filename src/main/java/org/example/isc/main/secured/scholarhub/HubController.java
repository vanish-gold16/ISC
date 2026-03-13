package org.example.isc.main.secured.scholarhub;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/scholar-hub")
public class HubController {

    @GetMapping
    public String getHub(
            Authentication authentication
    ){


        return "/private/scholar-hub";
    }

}
