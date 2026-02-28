package org.example.isc.main.secured.search;

import org.example.isc.main.secured.friends.service.FriendsService;
import org.example.isc.main.secured.models.Friends;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final UserRepository userRepository;
    private final FriendsService friendsService;

    public SearchController(UserRepository userRepository, FriendsService friendsService) {
        this.userRepository = userRepository;
        this.friendsService = friendsService;
    }

    @GetMapping
    public String search(
            @RequestParam(value = "q", required = false, defaultValue = "") String q,
            @RequestParam(value = "filter", required = false, defaultValue = "all") String filter,
            Model model,
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        List<User> allResults = q.isBlank()
                ? List.of()
                : userRepository.searchByQuery(q.trim())
                .stream()
                .filter(u -> !u.getId().equals(me.getId()))
                .collect(Collectors.toList());

        List<User> friends = friendsService.getAcceptedFriends(me);
        List<Long> friendsIds = friends.stream().map(User::getId).collect(Collectors.toList());

        List<User> friendResults = allResults.stream()
                .filter(u -> friendsIds.contains(u.getId()))
                .collect(Collectors.toList());

        model.addAttribute("q", q);
        model.addAttribute("filter", filter);
        model.addAttribute("allResults", allResults);
        model.addAttribute("friendResults", friendResults);
        model.addAttribute("friendIds", friendsIds);
        model.addAttribute("title", "Search");

        return "private/search";
    }

}
