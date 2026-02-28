package org.example.isc.main.secured.home;

import org.example.isc.main.secured.models.Post;
import org.example.isc.main.secured.repositories.LikeRepository;
import org.example.isc.main.secured.repositories.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeService {

    private static final Logger log = LoggerFactory.getLogger(HomeService.class);
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    public HomeService(PostRepository postRepository, LikeRepository likeRepository) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
    }

    public List<Post> getFeed(Long userId){
        List<Post> posts = postRepository.findFeed(userId);
        attachCounts(posts, userId);
        return posts;
    }

    private void attachCounts(List<Post> posts, Long viewerId) {
        for (Post post : posts) {
            Long postId = post.getId();
            post.setLikesCount(postId == null ? 0L : likeRepository.countByPostId(postId));
            post.setCommentsCount(0L);
            post.setSharesCount(0L);
            post.setLiked(postId != null && viewerId != null && likeRepository.existsByPostIdAndSenderId(postId, viewerId));
        }
    }
}