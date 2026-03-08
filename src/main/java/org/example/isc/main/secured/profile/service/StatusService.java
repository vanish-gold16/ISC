package org.example.isc.main.secured.profile.service;

import org.example.isc.main.dto.UserStatusDTO;
import org.example.isc.main.enums.PresenceStateEnum;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatusService {

    private final ActivityService activityService;

    public StatusService(ActivityService activityService) {
        this.activityService = activityService;
    }

    public Map<Long, UserStatusDTO> userStatuses(List<Long> userIds){
        Map<Long, UserStatusDTO> userAndStatus = new HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            if(activityService.online(userIds.get(i)))  userAndStatus.put(
                    (long) i,
                    new UserStatusDTO(PresenceStateEnum.ONLINE, Instant.now())
            );
            else if(!activityService.online((userIds.get(i)))) userAndStatus.put(
                    (long) i,
                    new UserStatusDTO(PresenceStateEnum.IDLE, Instant.now())
            );
            else userAndStatus.put(
                        (long) i,
                        new UserStatusDTO(PresenceStateEnum.OFFLINE, Instant.now()));
        }

        return userAndStatus;
    }

}
