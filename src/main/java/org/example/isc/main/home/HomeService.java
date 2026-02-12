package org.example.isc.main.home;

import org.example.isc.main.models.Post;
import org.example.isc.main.models.Subscription;
import org.example.isc.main.repositories.PostRepository;
import org.example.isc.main.repositories.SubscriptionRepository;
import org.example.isc.main.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

    private static final Logger log = LoggerFactory.getLogger(HomeService.class);
    private PostRepository postRepository;

    public HomeService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<Post> getFeed(Long userId){
        return postRepository.findFeed(userId);
    }

}
