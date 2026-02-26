package org.example.isc.main.secured.friends.service;

import jakarta.transaction.Transactional;
import org.example.isc.main.dto.FriendRequest;
import org.example.isc.main.enums.FriendsStatusEnum;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.Friends;
import org.example.isc.main.secured.models.Subscription;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.notification.NotificationService;
import org.example.isc.main.secured.repositories.FriendsRepository;
import org.example.isc.main.secured.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendsService {

    private final FriendsRepository friendsRepository;
    private final NotificationService notificationService;
    private final SubscriptionRepository subscriptionRepository;

    public FriendsService(FriendsRepository friendsRepository, NotificationService notificationService, SubscriptionRepository subscriptionRepository) {
        this.friendsRepository = friendsRepository;
        this.notificationService = notificationService;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public void sendFriendsRequest(
            User sender, User receiver
    ) {

        if (friendsRepository.existsBySenderUserAndRecieverUser(sender, receiver)) {
            return;
        }

        Friends friends = new Friends(
                sender, receiver, FriendsStatusEnum.PENDING
        );
        friendsRepository.save(friends);
        notificationService.create(
                NotificationEnum.FRIEND_REQUEST,
                receiver,
                sender,
                "Friend request",
                "wants to be your friend",
                null
        );
    }

    @Transactional
    public void acceptFriendRequest(
            User sender,
            User receiver
    ){
        Friends friends = friendsRepository.findBySenderUserAndRecieverUser(sender, receiver);

        if(friends != null) {
            friendsRepository.save(friends);
            friendsRepository.updateFriendshipStatus(sender, receiver, FriendsStatusEnum.ACCEPTED);
            String body = receiver.getUsername() + " has accepted your friend request";
            notificationService.create(
                NotificationEnum.FRIEND_REQUEST_ACCEPT,
                sender,
                receiver,
                "New friend!",
                body,
                null
            );
        }
    }

    @Transactional
    public void cancelFriendRequest(User  sender, User receiver){
        Friends friends = friendsRepository.findBySenderUserAndRecieverUserAndStatus(sender, receiver, FriendsStatusEnum.PENDING);
        if(friends != null)
            friends.setStatus(FriendsStatusEnum.DELETED);
    }

    public List<User> getAcceptedFriends(
            User target
    ){
        List<Friends> relations = friendsRepository.findAllFriendsByUser(target, FriendsStatusEnum.ACCEPTED);
        return relations.stream()
                .map(f -> f.getSenderUser().equals(target)
                                ? f.getRecieverUser()
                                : f.getSenderUser())
                .toList();
    }
}
