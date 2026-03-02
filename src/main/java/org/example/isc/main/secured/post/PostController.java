package org.example.isc.main.secured.post;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.isc.main.dto.NewCommentForm;
import org.example.isc.main.dto.NewPostForm;
import org.example.isc.main.enums.FriendsStatusEnum;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.*;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.post.CommentView;
import org.example.isc.main.secured.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PostController {

    private static final Logger log = LoggerFactory.getLogger(PostController.class);
    private final PostService postService;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final FriendsRepository friendsRepository;
    private final NotificationService notificationService;

    public PostController(PostService postService, UserRepository userRepository, LikeRepository likeRepository, PostRepository postRepository, CommentRepository commentRepository, NotificationService notificationService, FriendsRepository friendsRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
        this.friendsRepository = friendsRepository;
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

        List<Comment> flatComments = commentRepository.findAllByPostIdOrderByIdAsc(id);
        List<Friends> relations = friendsRepository.findAllFriendsByUser(me, FriendsStatusEnum.ACCEPTED);
        Long meId = me.getId();
        java.util.Set<Long> friendIds = relations.stream()
                .map(rel -> {
                    if (rel.getSenderUser() != null && rel.getSenderUser().getId() != null && !rel.getSenderUser().getId().equals(meId)) {
                        return rel.getSenderUser().getId();
                    }
                    if (rel.getRecieverUser() != null && rel.getRecieverUser().getId() != null && !rel.getRecieverUser().getId().equals(meId)) {
                        return rel.getRecieverUser().getId();
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Map<Long, CommentView> viewById = new LinkedHashMap<>();
        for (Comment comment : flatComments) {
            long likes = postService.getCommentLikeCounts(comment);
            boolean liked = postService.likedByUser(comment, me);
            long repliesCount = postService.getCommentRepliesCount(comment);
            String authorName = "ISC Member";
            String authorUsername = null;
            Long authorId = null;
            boolean friendAuthor = false;
            if (comment.getUser() != null) {
                String first = comment.getUser().getFirstName();
                String last = comment.getUser().getLastName();
                authorName = ((first != null ? first : "") + " " + (last != null ? last : "")).trim();
                if (authorName.isBlank()) {
                    authorName = "ISC Member";
                }
                authorUsername = comment.getUser().getUsername();
                if (comment.getUser().getId() != null) {
                    authorId = comment.getUser().getId();
                    friendAuthor = friendIds.contains(comment.getUser().getId());
                }
            }
            Long parentId = null;
            if (comment.getParentComment() != null) {
                parentId = comment.getParentComment().getId();
            }
            viewById.put(comment.getId(), new CommentView(
                    comment.getId(),
                    comment.getText(),
                    authorName,
                    authorUsername,
                    authorId,
                    friendAuthor,
                    likes,
                    liked,
                    repliesCount,
                    parentId
            ));
        }

        List<CommentView> commentViews = new ArrayList<>();
        for (CommentView view : viewById.values()) {
            if (view.getParentId() != null) {
                CommentView parentView = viewById.get(view.getParentId());
                if (parentView != null) {
                    parentView.addReply(view);
                    continue;
                }
            }
            commentViews.add(view);
        }

        model.addAttribute("commentForm", new NewCommentForm());
        model.addAttribute("commentViews", commentViews);
        log.info("Post {}: {} comments, {} view roots", id, flatComments.size(), commentViews.size());
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
                currentPost.getId() != null ? currentPost.getId().toString() : null
        );

        String target = (referer != null && !referer.isBlank()) ? referer : "/posts/" + id;
        return "redirect:" + target;
    }

}
