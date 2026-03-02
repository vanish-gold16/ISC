package org.example.isc.main.secured.search;

import org.example.isc.main.secured.friends.service.FriendsService;
import org.example.isc.main.secured.models.Friends;
import org.example.isc.main.secured.models.Subscription;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.SubscriptionRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final UserRepository userRepository;
    private final FriendsService friendsService;
    private final SubscriptionRepository subscriptionRepository;

    public SearchController(UserRepository userRepository, FriendsService friendsService, SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository;
        this.friendsService = friendsService;
        this.subscriptionRepository = subscriptionRepository;
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
        Set<Long> followingIds = subscriptionRepository.findByFollowerId(me.getId())
                .stream()
                .map(subscription -> subscription.getFollowed().getId())
                .collect(Collectors.toSet());

        List<User> friendResults = allResults.stream()
                .filter(u -> friendsIds.contains(u.getId()))
                .collect(Collectors.toList());

        model.addAttribute("q", q);
        model.addAttribute("filter", filter);
        model.addAttribute("allResults", allResults);
        model.addAttribute("friendResults", friendResults);
        model.addAttribute("friendIds", friendsIds);
        model.addAttribute("followingIds", followingIds);
        model.addAttribute("title", "Search");

        return "private/search";
    }

    @PostMapping("/{id}/subscribe")
    public String subscribe(
            @PathVariable Long id,
            Authentication authentication,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "filter", required = false, defaultValue = "all") String filter
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        User target = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found: " + id));

        boolean following = subscriptionRepository.existsByFollowedIdAndFollowerId(target.getId(), me.getId());
        if(!following){
            subscriptionRepository.save(new Subscription(me, target));
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/search");
        if (filter != null && !filter.isBlank()) {
            builder.queryParam("filter", filter);
        }
        if (q != null && !q.isBlank()) {
            builder.queryParam("q", q);
        }
        return "redirect:" + builder.build().toUriString();
    }

    @PostMapping(value = "/{id}/subscribe", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Void> followAjax(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        User target = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("User not found: " + authentication.getName()));

        boolean following = subscriptionRepository.existsByFollowedIdAndFollowerId(target.getId(), me.getId());
        if(!following){
            subscriptionRepository.save(new Subscription(me, target));
        }

        return ResponseEntity.ok().build();
    }

}
