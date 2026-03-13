package org.example.isc.main.secured.repositories;

import org.example.isc.main.secured.models.users.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
        select p from Post p
                where p.user.id = :userId
                order by p.date desc, p.id desc
        """)
    List<Post> findPostsByUserId(@Param("userId") Long userId);

    @Query("""
            select distinct p from Post p
            where p.user.id in (
                        select s.followed.id from Subscription s
                        where s.follower.id = :userId
                        )
               or p.user.id in (
                        select case
                                   when f.senderUser.id = :userId then f.recieverUser.id
                                   else f.senderUser.id
                               end
                        from Friends f
                        where f.status = org.example.isc.main.enums.FriendsStatusEnum.ACCEPTED
                          and (f.senderUser.id = :userId or f.recieverUser.id = :userId)
                        )
            order by p.date desc, p.id desc
                        """)
    List<Post> findFeed(
            @Param("userId") Long userId
    );

    List<Post> getPostById(Long id);

    Post findPostById(Long id);
}
