package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.users.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByPostId(Long postId);
    List<Comment> findAllByPostIdOrderByIdAsc(Long postId);

    long countByParentComment(Comment parentComment);
}
