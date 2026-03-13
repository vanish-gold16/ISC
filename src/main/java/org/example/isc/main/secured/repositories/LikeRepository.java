package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.users.Like;
import org.example.isc.main.secured.models.users.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    long countByPostId(Long postId);
    boolean existsByPostIdAndSenderId(Long postId, Long senderId);

    Like findByPostAndSenderId(Post post, Long senderId);
}