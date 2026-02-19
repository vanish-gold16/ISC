package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
        select p from Post p
                where p.user.id = :userId
        """)
    List<Post> findPostsByUserId(@Param("userId") Long userId);

    @Query("""
            select  p from Post p 
            where p.user.id in (
                        select s.followed.id from Subscription s
                        where s.follower.id = :userId           
                        )            
                        """)
    List<Post> findFeed(
            @Param("userId") Long userId
    );

}
