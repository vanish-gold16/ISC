package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.Comment;
import org.example.isc.main.secured.models.CommentLike;
import org.example.isc.main.secured.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    long countByComment(Comment comment);

    long countByCommentId(Long commentId);

    boolean existsByUserAndComment(User user, Comment comment);

    CommentLike findByUserAndComment(User user, Comment comment);

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    CommentLike findByCommentIdAndUserId(Long commentId, Long userId);
}
