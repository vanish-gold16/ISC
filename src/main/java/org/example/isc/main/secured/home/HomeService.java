package org.example.isc.main.secured.home;

import org.example.isc.main.secured.models.Post;
import org.example.isc.main.secured.repositories.PostRepository;
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
