package org.example.isc.main.secured.post;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.isc.main.dto.NewPostForm;
import org.example.isc.main.secured.models.Like;
import org.example.isc.main.secured.models.Post;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.LikeRepository;
import org.example.isc.main.secured.repositories.PostRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Controller
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public PostController(PostService postService, UserRepository userRepository, LikeRepository likeRepository, PostRepository postRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/post")
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

    @PostMapping("/post")
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

    @PostMapping("/posts/{id}/like")
    public String likePost(
            @PathVariable Long id,
            Authentication authentication,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        Post currentPost = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id));

        if (likeRepository.existsByPostIdAndSenderId(id, me.getId())) {
            Like like = likeRepository.findByPostAndSenderId(currentPost, me.getId());
            if (like != null) {
                likeRepository.delete(like);
            }
        } else {
            Like like = new Like(me, currentPost);
            likeRepository.save(like);
        }

        String target = (referer != null && !referer.isBlank()) ? referer : "/profile";
        return "redirect:" + target;
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