package org.example.isc.main.secured.profile.service;

import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.models.users.UserProfile;
import org.example.isc.main.secured.profile.dto.PresenceEvent;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActivityService {

    private static final Duration ONLINE_THRESHOLD = Duration.ofMinutes(1);

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final Set<Long> activeUsers = ConcurrentHashMap.newKeySet();

    public ActivityService(UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public boolean online(@RequestParam Long id){
        if (activeUsers.contains(id)) {
            LocalDateTime lastActivity = userRepository.findById(id)
                    .map(User::getProfile)
                    .map(UserProfile::getLastActivityAt)
                    .orElse(null);
            if (lastActivity == null) {
                activeUsers.remove(id);
                return false;
            }
            boolean stillOnline = Duration.between(lastActivity, LocalDateTime.now())
                    .compareTo(ONLINE_THRESHOLD) <= 0;
            if (!stillOnline) {
                activeUsers.remove(id);
            }
            return stillOnline;
        }
        LocalDateTime lastActivity = userRepository.findById(id)
                .map(User::getProfile)
                .map(UserProfile::getLastActivityAt)
                .orElse(null);
        return lastActivity != null && Duration.between(lastActivity, LocalDateTime.now()).compareTo(ONLINE_THRESHOLD) <= 0;
    }

    public void touch(User user) {
        LocalDateTime now = LocalDateTime.now();
        UserProfile profile = ensureProfile(user);
        profile.setLastActivityAt(now);
        userRepository.save(user);
        activeUsers.add(user.getId());
        broadcast(user.getId(), true, now);
    }

    public void leave(User user) {
        LocalDateTime lastSeen = LocalDateTime.now();
        UserProfile profile = ensureProfile(user);
        profile.setLastActivityAt(lastSeen);
        userRepository.save(user);
        activeUsers.remove(user.getId());
        broadcast(user.getId(), false, lastSeen);
    }

    private void broadcast(Long userId, boolean online, LocalDateTime lastSeenAt) {
        PresenceEvent event = new PresenceEvent(userId, online, lastSeenAt);
        messagingTemplate.convertAndSend("/topic/presence", event);
    }

    private UserProfile ensureProfile(User user) {
        if (user.getProfile() == null) {
            UserProfile profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        }
        return user.getProfile();
    }

}
