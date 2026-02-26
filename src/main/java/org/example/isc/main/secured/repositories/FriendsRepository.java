package org.example.isc.main.secured.repositories;

import org.example.isc.main.enums.FriendsStatusEnum;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.Friends;
import org.example.isc.main.secured.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendsRepository extends JpaRepository<Friends, Long> {

    List<Friends> findAllByRecieverUser(User recieverUser);

    boolean existsBySenderUserAndRecieverUser(User sender, User receiver);

    @Query("""
        select f from Friends f
        where f.status = :status
          and (f.senderUser = :user or f.recieverUser = :user)
        """)
    List<Friends> findAllFriendsByUser(@Param("user") User user, @Param("status") FriendsStatusEnum status);

    Friends findBySenderUserAndRecieverUser(User senderUser, User recieverUser);

    @Modifying
    @Query("""
        update Friends f 
                set f.status = :status
                where f.senderUser = :sender
                and f.recieverUser = :receiver        
        """)
    void updateFriendshipStatus(User sender, User receiver, FriendsStatusEnum status);

    boolean existsBySenderUserAndRecieverUserOrRecieverUserAndSenderUser(User senderUser, User recieverUser, User recieverUser1, User senderUser1);

    boolean existsBySenderUserAndRecieverUserAndStatus(User senderUser, User recieverUser, FriendsStatusEnum status);

    Friends findBySenderUserAndRecieverUserAndStatus(User senderUser, User recieverUser, FriendsStatusEnum status);
}
