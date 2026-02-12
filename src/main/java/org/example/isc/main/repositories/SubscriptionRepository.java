package org.example.isc.main.repositories;


import org.example.isc.main.models.Subscription;
import org.example.isc.main.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

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

}
