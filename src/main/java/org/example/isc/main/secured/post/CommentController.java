package org.example.isc.main.secured.post;

import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.Comment;
import org.example.isc.main.secured.models.CommentLike;
import org.example.isc.main.secured.models.Post;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/posts")
public class CommentController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final NotificationsRepository notificationsRepository;
    private final NotificationService notificationService;

    public CommentController(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository, CommentLikeRepository commentLikeRepository, NotificationsRepository notificationsRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.notificationsRepository = notificationsRepository;
        this.notificationService = notificationService;
    }

    @PostMapping("/{id}/comments/{id}/like")
    public String likeCommnt(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication,
            Model model
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        Comment currentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalStateException("Comment not found: " + commentId));

        boolean isLikedComment = commentLikeRepository.existsByCommentIdAndUserId(commentId, me.getId());
        if(isLikedComment) {
            CommentLike commentLike = commentLikeRepository.findByUserAndComment(
                    me, currentComment
            );
            commentLikeRepository.delete(commentLike);
        } else{
            CommentLike commentLike = new CommentLike(me, currentComment);
            commentLikeRepository.save(commentLike);
            notificationService.create(
                    NotificationEnum.COMMENT,
                    currentComment.getUser(),
                    me,
                    "New comment like!",
                    " has liked your comment",
                    null
            );
        }

        return "redirect:/posts/" + postId;
    }

    @PostMapping(value = "/{id}/comments/{id}/like")
    @ResponseBody
    public ResponseEntity<Void> likeCommentAjax(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication,
            Model model
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        Post currentPost = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found: " + id));

        Comment currentComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalStateException("Comment not found: " + commentId));

        boolean isLikedComment = commentLikeRepository.existsByCommentIdAndUserId(commentId, me.getId());
        if(isLikedComment) {
            CommentLike commentLike = commentLikeRepository.findByUserAndComment(
                    me, currentComment
            );
            commentLikeRepository.delete(commentLike);
        } else{
            CommentLike commentLike = new CommentLike(me, currentComment);
            commentLikeRepository.save(commentLike);
            notificationService.create(
                    NotificationEnum.COMMENT,
                    currentComment.getUser(),
                    me,
                    "New comment like!",
                    " has liked your comment",
                    null
            );
        }

        return ResponseEntity.ok().build();
    }

}
