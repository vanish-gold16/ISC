package org.example.isc.settings;

import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.settings.dto.UserSettingsDTO;
import org.example.isc.settings.repository.UserSettingsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/settings")
public class SettingsApiController {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    public SettingsApiController(UserRepository userRepository, UserSettingsRepository userSettingsRepository) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
    }

    @GetMapping
    public ResponseEntity<List<UserSettingsDTO>> getSettings(
            Authentication authentication
    ){
        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        UserSettings settings = userSettingsRepository.findByUserId(me.getId());
        if(settings == null){
            return ResponseEntity.badRequest().build();
        }


    }

    private UserSettingsDTO toDTO(UserSettings settings){
        return new UserSettingsDTO(
                userSettingsRepository.findByUserId(settings.getId()).getUserId(),

        );
    }

}
