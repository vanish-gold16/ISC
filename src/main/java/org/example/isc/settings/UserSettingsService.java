package org.example.isc.settings;

import org.example.isc.main.enums.scholarhub.GradingSystemEnum;
import org.example.isc.settings.dto.AppearanceSettingsDTO;
import org.example.isc.settings.dto.NotificationSettingsDTO;
import org.example.isc.settings.dto.ScholarHubSettingsDTO;
import org.example.isc.settings.dto.UserSettingsDTO;
import org.example.isc.settings.enums.ThemeEnum;
import org.example.isc.settings.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;

    public UserSettingsService(UserSettingsRepository userSettingsRepository) {
        this.userSettingsRepository = userSettingsRepository;
    }

    public UserSettingsDTO getSettingsForUser(Long userId){
        UserSettings settings = userSettingsRepository.findByUserId(userId);

        if(settings == null){
            AppearanceSettingsDTO appearanceSettings = new AppearanceSettingsDTO(
                    ThemeEnum.Light,
                    false
            );
            NotificationSettingsDTO notificationSettings = new NotificationSettingsDTO(
                    true,
                    true
            );
            ScholarHubSettingsDTO scholarHubSettings = new ScholarHubSettingsDTO(
                    GradingSystemEnum.Letter_Grading
            );

            UserSettingsDTO userSettings = new UserSettingsDTO(
                    scholarHubSettings,
                    appearanceSettings,
                    notificationSettings
            );

            userSettingsRepository.save(toSettings(userSettings, userId));

            return userSettings;
        }

    }

    public UserSettingsDTO saveSettingsForUser(Long userId, UserSettingsDTO dto){



    }

    private UserSettings toSettings(UserSettingsDTO dto, Long userId){
        UserSettings settings = userSettingsRepository.findByUserId(userId);
        settings.setSettingsJson(dto.toString());
        return settings;
    }

}
