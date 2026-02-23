package org.example.isc.main.secured.friends.service;

import org.example.isc.main.dto.FriendRequest;
import org.example.isc.main.enums.FriendsStatusEnum;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.Friends;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.repositories.FriendsRepository;
import org.springframework.stereotype.Service;

@Service
public class FriendsService {

    private final FriendsRepository friendsRepository;
    private final NotificationService notificationService;

    public FriendsService(FriendsRepository friendsRepository, NotificationService notificationService) {
        this.friendsRepository = friendsRepository;
        this.notificationService = notificationService;
    }

    public void sendFriendsRequest(
            User sender, User receiver
    ) {
        Friends friends = new Friends(
                sender, receiver, FriendsStatusEnum.PENDING
        );
        String body = sender.getUsername() + " has sent you a friend request";
        friendsRepository.save(friends);
        notificationService.create(
                NotificationEnum.FRIEND_REQUEST,
                receiver,
                "New friend request",
                body,
                "New friend request"
        );
    }
}
