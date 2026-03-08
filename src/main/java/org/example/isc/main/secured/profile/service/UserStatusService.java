package org.example.isc.main.secured.profile.service;

import org.example.isc.main.dto.UserStatusDTO;
import org.example.isc.main.enums.PresenceStateEnum;
import org.example.isc.main.secured.models.User;
import org.example.isc.main.secured.models.UserProfile;
import org.example.isc.main.secured.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserStatusService {

    private final ActivityService activityService;
    private final UserRepository userRepository;

    public UserStatusService(ActivityService activityService, UserRepository userRepository) {
        this.activityService = activityService;
        this.userRepository = userRepository;
    }

    public Map<Long, UserStatusDTO> getStatuses(List<Long> userIds){
        Map<Long, UserStatusDTO> userAndStatus = new HashMap<>();
        List<User> users = userRepository.findAllById(userIds);

        for (User user : users) {
            UserProfile profile = user.getProfile();
            LocalDateTime lastActivity = profile == null ? null : profile.getLastActivityAt();

            PresenceStateEnum state;
            if(activityService.online(user.getId()))
                state = PresenceStateEnum.ONLINE;
            else if(!activityService.online((user.getId())))
                state = PresenceStateEnum.IDLE;
            else state = PresenceStateEnum.OFFLINE;

            Instant lastActivityInstant = lastActivity == null ? null
                    : lastActivity.toInstant(ZoneOffset.UTC);
            userAndStatus.put(user.getId(), new UserStatusDTO(state, lastActivityInstant));
        }
        return userAndStatus;
    }

}
