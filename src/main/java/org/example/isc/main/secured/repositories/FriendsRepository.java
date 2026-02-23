package org.example.isc.main.secured.repositories;

import org.example.isc.main.enums.FriendsStatusEnum;
import org.example.isc.main.secured.models.Friends;
import org.example.isc.main.secured.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendsRepository extends JpaRepository<Friends, Long> {

    List<Friends> findAllByRecieverUser(User recieverUser);

    @Query("""
        select f from Friends f
        where f.status = :status
          and (f.senderUser = :user or f.recieverUser = :user)
        """)
    List<Friends> findAllFriendsByUser(@Param("user") User user, @Param("status") FriendsStatusEnum status);
}
