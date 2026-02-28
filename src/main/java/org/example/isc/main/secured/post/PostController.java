package org.example.isc.main.secured.post;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.isc.main.dto.EditRequest;
import org.example.isc.main.dto.NewPostForm;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.profile.service.ProfileService;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    public PostController(PostService postService, UserRepository userRepository, ProfileService profileService) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    @GetMapping
    public String newPost(
            @Valid @ModelAttribute("form") NewPostForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            Authentication authentication
    ){
        if(bindingResult.hasErrors()) {
            User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                    .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
            model.addAttribute("title", "New post");
            model.addAttribute("user", me);
            model.addAttribute("title", form.getTitle());
            model.addAttribute("body", form.getBody());
            model.addAttribute("postTime", LocalDateTime.now());
            // photo
            return "private/new-post";
        } try{
            postService.newPost(authentication, form);
        } catch(IllegalArgumentException e){
            User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                    .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
            model.addAttribute("title", "New post");
            model.addAttribute("user", me);
            model.addAttribute("title", form.getTitle());
            model.addAttribute("body", form.getBody());
            model.addAttribute("postTime", LocalDateTime.now());
        }

        session.setAttribute("POST_NEW_POST", true);

        return "private/profile";
    }

}
