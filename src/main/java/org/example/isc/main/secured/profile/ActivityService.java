package org.example.isc.main.secured.profile;

import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ActivityService {

    private final UserRepository userRepository;

    public ActivityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean online(@RequestParam Long id){
        User target = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        boolean online = target.getProfile().getLastActivityAt() != null
                && Duration.between(target.getProfile().getLastActivityAt(), LocalDateTime.now()).toMinutes() <= 1;

        return online;
    }

}
