package org.example.isc.main.secured.friends.controller;

import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.FriendsRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/friends")
public class FriendsController {

    private final FriendsRepository friendsRepository;
    private final UserRepository userRepository;

    public FriendsController(FriendsRepository friendsRepository, UserRepository userRepository) {
        this.friendsRepository = friendsRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String friends(
            Model model,
            Authentication authentication
            ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                        .orElseThrow(() ->
                                new IllegalStateException("Logged-in user not found: " + authentication.getName())
                        );

        model.addAttribute("title", "Friends");
        model.addAttribute("friends", friendsRepository.findAllByRecieverUser(me));

        return "/private/friends";
    }

}
