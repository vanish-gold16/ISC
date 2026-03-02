package org.example.isc.main.secured.post;

import jakarta.validation.Valid;
import org.example.isc.main.dto.NewCommentForm;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.Comment;
import org.example.isc.main.secured.models.Post;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.post.PostService;
import org.example.isc.main.secured.repositories.CommentRepository;
import org.example.isc.main.secured.repositories.PostRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Controller
@RequestMapping("/posts")
public class CommentController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final PostService postService;

    public CommentController(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository, NotificationService notificationService, PostService postService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
        this.postService = postService;
    }

    @PostMapping("/{postId}/comments/{commentId}/like")
    public String likeCommnt(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication,
            @RequestHeader(value = "Referer", required = false) String referer
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        Comment currentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalStateException("Comment not found: " + commentId));

        boolean liked = postService.toggleCommentLike(me, currentComment);
        if (liked) {
            notificationService.create(
                    NotificationEnum.LIKE,
                    currentComment.getUser(),
                    me,
                    "New comment like!",
                    " has liked your comment",
                    null
            );
        }

        String target = (referer != null && !referer.isBlank()) ? referer : "/posts/" + postId;
        return "redirect:" + target;
    }

    @PostMapping(value = "/{postId}/comments/{commentId}/like", produces = "application/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> likeCommentAjax(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        Comment currentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalStateException("Comment not found: " + commentId));

        boolean liked = postService.toggleCommentLike(me, currentComment);
        if (liked) {
            notificationService.create(
                    NotificationEnum.LIKE,
                    currentComment.getUser(),
                    me,
                    "New comment like!",
                    " has liked your comment",
                    null
            );
        }

        Map<String, Object> payload = Map.of(
                "liked", liked,
                "likesCount", postService.getCommentLikeCounts(currentComment)
        );
        return ResponseEntity.ok(payload);
    }

    @PostMapping("/{postId}/comment/{commentId}/reply")
    public String replyComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication,
            @Valid @ModelAttribute NewCommentForm form,
            BindingResult result,
            @RequestHeader(value = "Referer", required = false) String referer
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        Post currentPost = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + postId));

        Comment currentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalStateException("Comment not found: " + commentId));

        if (result.hasErrors() || form.getText() == null || form.getText().isBlank()) {
            String target = (referer != null && !referer.isBlank()) ? referer : "/posts/" + postId;
            return "redirect:" + target;
        }

        Comment comment = new Comment(
                currentPost,
                me,
                form.getText(),
                currentComment
        );
        commentRepository.save(comment);
        notificationService.create(
                NotificationEnum.COMMENT,
                currentComment.getUser(),
                me,
                "New comment reply",
                " has replied to your comment",
                currentPost.getId() != null ? currentPost.getId().toString() : null
        );

        String target = (referer != null && !referer.isBlank()) ? referer : "/posts/" + postId;
        return "redirect:" + target;
    }

}
