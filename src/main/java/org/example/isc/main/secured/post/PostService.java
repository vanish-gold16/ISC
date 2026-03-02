package org.example.isc.main.secured.post;

import jakarta.transaction.Transactional;
import org.example.isc.cloudinary.ImageService;
import org.example.isc.main.dto.NewPostForm;
import org.example.isc.main.secured.models.Comment;
import org.example.isc.main.secured.models.CommentLike;
import org.example.isc.main.secured.models.Post;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.CommentLikeRepository;
import org.example.isc.main.secured.repositories.CommentRepository;
import org.example.isc.main.secured.repositories.PostRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository, ImageService imageService, CommentLikeRepository commentLikeRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.commentLikeRepository = commentLikeRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void newPost(
            Authentication authentication,
            NewPostForm form
    ) throws IOException {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));

        String photoUrl = null;
        if (form.getPhoto() != null && !form.getPhoto().isEmpty()) {
            photoUrl = imageService.uploadPostImage(form.getPhoto(), me.getId());
        }

        Post post = new Post(
                me,
                form.getTitle(),
                form.getBody(),
                LocalDateTime.now()
        );
        post.setPhotoUrl(photoUrl);

        postRepository.save(post);
    }

    public boolean likedByUser(
            Comment comment, User user
    ){
        if (comment == null || user == null) {
            return false;
        }
        return commentLikeRepository.existsByUserAndComment(user, comment);
    }

    public long getCommentLikeCounts(
            Comment comment
    ){
        if (comment == null) {
            return 0;
        }
        return commentLikeRepository.countByComment(comment);
    }

    public long getCommentRepliesCount(
            Comment comment
    ){
        if (comment == null) {
            return 0;
        }
        return commentRepository.countByParentComment(comment);
    }

    @Transactional
    public boolean toggleCommentLike(
            User user, Comment comment
    ){
        if (user == null || comment == null) {
            throw new IllegalArgumentException("User and comment must be provided");
        }
        CommentLike existing = commentLikeRepository.findByUserAndComment(user, comment);
        if (existing != null) {
            commentLikeRepository.delete(existing);
            return false;
        }

        commentLikeRepository.save(new CommentLike(user, comment));
        return true;
    }

}
