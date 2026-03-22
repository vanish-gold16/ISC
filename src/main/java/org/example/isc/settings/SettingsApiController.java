package org.example.isc.settings;

import lombok.extern.slf4j.Slf4j;
import org.example.isc.main.secured.models.users.User;
import org.example.isc.main.secured.repositories.UserRepository;
import org.example.isc.settings.dto.UserSettingsDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/settings")
public class SettingsApiController {

    private final UserRepository userRepository;
    private final UserSettingsService userSettingsService;

    public SettingsApiController(UserRepository userRepository, UserSettingsService userSettingsService) {
        this.userRepository = userRepository;
        this.userSettingsService = userSettingsService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserSettingsDTO> getSettings(Authentication authentication) {
        return ResponseEntity.ok(userSettingsService.getSettingsForUser(requireCurrentUserId(authentication)));
    }

    @PutMapping("/me")
    public ResponseEntity<UserSettingsDTO> saveSettings(
            Authentication authentication,
            @RequestBody UserSettingsDTO settings
    ) {
        return ResponseEntity.ok(userSettingsService.saveSettingsForUser(requireCurrentUserId(authentication), settings));
    }

    private Long requireCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required.");
        }

        User me = userRepository.findByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Logged-in user not found: " + authentication.getName()));

        return me.getId();
    }
}
