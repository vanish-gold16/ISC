package org.example.isc.main.secured.friends.service;

import org.example.isc.main.secured.repositories.FriendsRepository;
import org.springframework.stereotype.Service;

@Service
public class FriendsService {

    private final FriendsRepository friendsRepository;

    public FriendsService(FriendsRepository friendsRepository) {
        this.friendsRepository = friendsRepository;
    }

    public void sendFriendsRequest() {

    }
}
