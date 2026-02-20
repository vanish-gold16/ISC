package org.example.isc.main.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class LandingController {

    @GetMapping
    public String getHomePage(){
        return "public/landing";
    }

}
