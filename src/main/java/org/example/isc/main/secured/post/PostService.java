package org.example.isc.main.secured.post;

import jakarta.transaction.Transactional;
import org.example.isc.cloudinary.ImageService;
import org.example.isc.main.dto.NewPostForm;
import org.example.isc.main.secured.models.Post;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.PostRepository;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    public PostService(PostRepository postRepository, UserRepository userRepository, ImageService imageService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    @Transactional
    public void newPost(
            Authentication authentication,
            NewPostForm form
            ) throws IOException {
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found " + authentication.getName()));

        String photoUrl = null;
        if(form.getPhoto() != null && !form.getBody().isEmpty())
            photoUrl = imageService.uploadPostImage(form.getPhoto(), me.getId());

        Post post = new Post(
                me,
                form.getTitle(),
                form.getBody(),
                LocalDateTime.now()
        );

        post.setPhotoUrl(photoUrl);

        postRepository.save(post);
    }

}
