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

    // // TODO
    //
    //          Как сделать, чтобы лайк срабатывал (минимальный, понятный путь, делай сам):
    //
    //          1. Роут:
    //              - POST /posts/{id}/like — тумблер (ставит/снимает).
    //          2. Сервис:
    //              - Если LikeRepository.existsByPostIdAndSenderId(postId, userId) — удалить лайк.
    //              - Иначе — создать Like и сохранить.
    //          3. Отображение:
    //              - Лайки считаются LikeRepository.countByPostId(postId); ты это уже прокидываешь в likesCount.
    //          4. Кнопка в шаблоне:
    //              - <form method="post" th:action="@{/posts/{id}/like(id=${p.id})}">
    //              - <button type="submit" class="action">…</button>
    //              - Не забудь CSRF.
    //          5. Иконка:
    //              - Меняй src по p.liked (у тебя уже есть флаг). Если liked=true, показывай liked.png, иначе like.png.
    //
    //          Если хочешь, покажи текущие контроллер/сервис для лайков — помогу сделать тумблер без лишнего кода.

}