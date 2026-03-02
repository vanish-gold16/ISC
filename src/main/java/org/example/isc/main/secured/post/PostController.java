package org.example.isc.main.secured.post;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.isc.main.dto.NewCommentForm;
import org.example.isc.main.dto.NewPostForm;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.*;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationsRepository notificationsRepository;
    private final NotificationService notificationService;

    public PostController(PostService postService, UserRepository userRepository, LikeRepository likeRepository, PostRepository postRepository, CommentRepository commentRepository, NotificationsRepository notificationsRepository, NotificationService notificationService) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.notificationsRepository = notificationsRepository;
        this.notificationService = notificationService;
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

    @GetMapping("/posts/{id}")
    public String fullPost(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        Post currentPost = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id));

        currentPost.setLikesCount(likeRepository.countByPostId(id));
        currentPost.setCommentsCount(commentRepository.countByPostId(id));
        currentPost.setLiked(likeRepository.existsByPostIdAndSenderId(id, me.getId()));

        model.addAttribute("commentForm", new NewCommentForm());
        model.addAttribute("comments", commentRepository.findAllByPostIdOrderByIdAsc(id));
        model.addAttribute("title", currentPost.getUser().getUsername());
        model.addAttribute("post", currentPost);

        return "private/post";
    }

    @GetMapping("/posts/{id}/to-profile")
    public String toProfile(
            @PathVariable Long id
    ){
        Post currentPost = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id));
        if (currentPost.getUser() == null || currentPost.getUser().getId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post owner not found: " + id);
        }
        return "redirect:/profile/" + currentPost.getUser().getId();
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
            notificationService.create(NotificationEnum.LIKE, currentPost.getUser(), me, "New like",
                    " has liked your post", null);
        }

        String target = (referer != null && !referer.isBlank()) ? referer : "/profile";
        return "redirect:" + target;
    }

    @PostMapping(value = "/posts/{id}/like", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> likePostAjax(
            @PathVariable Long id,
            Authentication authentication
    ) {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        Post currentPost = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id));

        boolean liked;
        if (likeRepository.existsByPostIdAndSenderId(id, me.getId())) {
            Like like = likeRepository.findByPostAndSenderId(currentPost, me.getId());
            if (like != null) {
                likeRepository.delete(like);
            }
            liked = false;
        } else {
            Like like = new Like(me, currentPost);
            likeRepository.save(like);
            liked = true;
        }

        long likesCount = likeRepository.countByPostId(id);
        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "likesCount", likesCount
        ));
    }

    @PostMapping("/posts/{id}/comment")
    public String comment(
            @PathVariable Long id,
            Authentication authentication,
            @Valid @ModelAttribute("commentForm") NewCommentForm form,
            BindingResult bindingResult,
            @RequestHeader(value = "Referer", required = false) String referer
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        Post currentPost = postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id));

        if (bindingResult.hasErrors() || form.getText() == null || form.getText().isBlank()) {
            String target = (referer != null && !referer.isBlank()) ? referer : "/posts/" + id;
            return "redirect:" + target;
        }

        Comment comment = new Comment(currentPost, me, form.getText().trim());
        commentRepository.save(comment);
        notificationService.create(
                NotificationEnum.COMMENT,
                currentPost.getUser(),
                me,
                "New comment",
                comment.getText(),
                null
        );

        String target = (referer != null && !referer.isBlank()) ? referer : "/posts/" + id;
        return "redirect:" + target;
    }

}
