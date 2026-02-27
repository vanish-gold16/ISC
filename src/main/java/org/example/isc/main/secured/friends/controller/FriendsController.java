package org.example.isc.main.secured.friends.controller;

import org.example.isc.main.secured.friends.service.FriendsService;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.FriendsRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/friends")
public class FriendsController {

    private final FriendsRepository friendsRepository;
    private final UserRepository userRepository;
    private final FriendsService friendsService;

    public FriendsController(FriendsRepository friendsRepository, UserRepository userRepository, FriendsService friendsService) {
        this.friendsRepository = friendsRepository;
        this.userRepository = userRepository;
        this.friendsService = friendsService;
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

        List<User> friends = friendsService.getAcceptedFriends(me);

        model.addAttribute("title", "Friends");
        model.addAttribute("friends", friends);

        return "private/friends";
    }

}
