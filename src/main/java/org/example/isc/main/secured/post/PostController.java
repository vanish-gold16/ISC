package org.example.isc.main.secured.post;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.isc.main.dto.NewPostForm;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/post")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    public PostController(PostService postService, UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String newPost(Model model, Authentication authentication) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));
        model.addAttribute("title", "New post");
        model.addAttribute("user", me);
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new NewPostForm());
        }
        return "private/new-post";
    }

    @PostMapping
    public String newPost(
            @Valid @ModelAttribute("form") NewPostForm form,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            Authentication authentication
    ) {
        if (bindingResult.hasErrors()) {
            return newPost(model, authentication);
        }
        try {
            postService.newPost(authentication, form);
        } catch (IllegalArgumentException | IOException e) {
            model.addAttribute("error", e.getMessage());
            return newPost(model, authentication);
        }

        session.setAttribute("POST_NEW_POST", true);
        return "redirect:/profile";
    }

}