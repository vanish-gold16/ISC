package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
