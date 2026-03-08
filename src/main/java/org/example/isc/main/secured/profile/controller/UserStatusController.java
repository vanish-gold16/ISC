package org.example.isc.main.secured.profile.controller;

import org.example.isc.main.dto.UserStatusDTO;
import org.example.isc.main.secured.profile.service.StatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserStatusController {

    private final StatusService statusService;

    public UserStatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/statuses")
    public Map<Long, UserStatusDTO> list(@RequestParam List<Long> userIds){
        return statusService.userStatuses(userIds);
    }

}
