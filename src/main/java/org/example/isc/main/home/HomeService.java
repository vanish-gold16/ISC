package org.example.isc.main.home;

import org.example.isc.main.models.Post;
import org.example.isc.main.repositories.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

    private static final Logger log = LoggerFactory.getLogger(HomeService.class);
    private SubscriptionRepository subscriptionRepository;

    public HomeService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public void getSubscriptionList(Long userId){
        subscriptionRepository.findSubscriptionByFollowedId(userId);
    }

    public List<Post> getPostsToHomePage(){

    }

}
