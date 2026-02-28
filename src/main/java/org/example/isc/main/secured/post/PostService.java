package org.example.isc.main.secured.post;

import jakarta.transaction.Transactional;
import org.example.isc.main.dto.NewPostForm;
import org.example.isc.main.secured.models.Post;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.PostRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void newPost(
            Authentication authentication,
            NewPostForm form
            ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));

        Post post = new Post(
                me,
                form.getTitle(),
                form.getBody(),
                LocalDateTime.now()
        );

        postRepository.save(post);
    }

}
