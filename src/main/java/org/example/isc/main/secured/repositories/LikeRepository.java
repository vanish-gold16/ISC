package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.Like;
import org.example.isc.main.secured.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeRepository extends JpaRepository<Like, Long> {
    long countByPostId(Long postId);
    boolean existsByPostIdAndSenderId(Long postId, Long senderId);

    Like findByPostAndSenderId(Post post, Long senderId);
}