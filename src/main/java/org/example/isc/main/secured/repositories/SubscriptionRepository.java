package org.example.isc.main.secured.repositories;


import org.example.isc.main.secured.models.Subscription;
import org.example.isc.main.secured.models.User;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    long countByFollowerId(Long followerId);
    long countByFollowedId(Long followedId);

    @Query("""
        select s from Subscription s
                where s.follower.id = :followerId
                """)
    List<Subscription> findByFollowerId(
            @Param("followedId") Long followedId
    );

    @Query("""
        select s.follower from Subscription s
                where s.followed.id = :followedId
                """)
    List<User> findByFollowedId(
            @Param("followedId") Long followedId
    );

    boolean findByFollowed(User followed);

    @Query("select s from Subscription s where s.followed.id = :followedId and s.follower.id = :followerId order by s.id asc limit 1")
    List<Subscription> findByFollowedIdAndFollowerId(Long followedId, Long followerId);

    boolean existsByFollowedIdAndFollowerId(Long followedId, Long followerId);

    List<Subscription> findAllByFollowedIdAndFollowerId(Long followedId, Long followerId);

    List<Subscription> findAllByFollowedIdAndFollowerUsernameIgnoreCase(Long followedId, String followerUsername);
}
