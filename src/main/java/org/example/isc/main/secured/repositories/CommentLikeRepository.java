package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.Comment;
import org.example.isc.main.secured.models.CommentLike;
import org.example.isc.main.secured.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    long countCommentLikeByComment(Comment comment);

    Long existsCommentLikeByUser(User user);

    Long existsCommentLikeByUserAndComment(User user, Comment comment);
}
