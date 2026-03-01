package org.example.isc.main.secured.home;

import org.example.isc.main.enums.FriendsStatusEnum;
import org.example.isc.main.secured.models.Friends;
import org.example.isc.main.secured.models.Post;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.FriendsRepository;
import org.example.isc.main.secured.repositories.LikeRepository;
import org.example.isc.main.secured.repositories.CommentRepository;
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
    private final FriendsRepository friendsRepository;
    private final CommentRepository commentRepository;

    public HomeService(PostRepository postRepository, LikeRepository likeRepository, FriendsRepository friendsRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.friendsRepository = friendsRepository;
        this.commentRepository = commentRepository;
    }

    public List<Post> getFeed(User me){
        List<Post> posts = postRepository.findFeed(me.getId());
        attachCounts(posts, me);
        return posts;
    }

    private void attachCounts(List<Post> posts, User viewer) {
        Long viewerId = viewer == null ? null : viewer.getId();
        List<Friends> relations = viewer == null
                ? List.of()
                : friendsRepository.findAllFriendsByUser(viewer, FriendsStatusEnum.ACCEPTED);
        List<Long> friendIds = relations.stream()
                .map(f -> f.getSenderUser().getId().equals(viewerId) ? f.getRecieverUser().getId() : f.getSenderUser().getId())
                .toList();
        for (Post post : posts) {
            Long postId = post.getId();
            Long authorId = post.getUser() == null ? null : post.getUser().getId();
            post.setLikesCount(postId == null ? 0L : likeRepository.countByPostId(postId));
            post.setCommentsCount(postId == null ? 0L : commentRepository.countByPostId(postId));
            post.setSharesCount(0L);
            post.setLiked(postId != null && viewerId != null && likeRepository.existsByPostIdAndSenderId(postId, viewerId));
            post.setFriendPost(authorId != null && viewerId != null && friendIds.contains(authorId));
        }
    }
}
