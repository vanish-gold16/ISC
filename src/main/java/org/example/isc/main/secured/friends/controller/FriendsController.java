package org.example.isc.main.secured.friends.controller;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/friends")
public class FriendsController {

    @GetMapping
    public String friends(
            Model model,
            Authentication authentication
            ){
        model.addAttribute("title", "Friends");
        //TODO friends logic

        return "/private/friends";
    }

}
