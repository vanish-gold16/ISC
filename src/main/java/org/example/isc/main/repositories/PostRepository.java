package org.example.isc.main.repositories;

import jakarta.transaction.Transactional;
import org.example.isc.main.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
        select p from Post p
                where p.user = :userId
        """)
    List<Long> findPostsByUserId(@Param("userId") Long userId);

}
