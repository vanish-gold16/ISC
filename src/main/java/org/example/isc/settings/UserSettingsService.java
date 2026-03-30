package org.example.isc.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.isc.main.enums.scholarhub.GradingSystemEnum;
import org.example.isc.settings.dto.AppearanceSettingsDTO;
import org.example.isc.settings.dto.NotificationSettingsDTO;
import org.example.isc.settings.dto.ScholarHubSettingsDTO;
import org.example.isc.settings.dto.UserSettingsDTO;
import org.example.isc.settings.enums.LessonEnum;
import org.example.isc.settings.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserSettingsService {
    private static final Pattern LESSON_TIME_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    private final UserSettingsRepository userSettingsRepository;
    private final ObjectMapper objectMapper;

    public UserSettingsService(UserSettingsRepository userSettingsRepository, ObjectMapper objectMapper) {
        this.userSettingsRepository = userSettingsRepository;
        this.objectMapper = objectMapper;
    }

    public UserSettingsDTO getSettingsForUser(Long userId) {
        UserSettings settings = userSettingsRepository.findByUserId(userId);
        if (settings == null || settings.getSettingsJson() == null || settings.getSettingsJson().isBlank()) {
            UserSettingsDTO defaults = defaultSettings();
            log.info("Default settings saved");
            userSettingsRepository.save(new UserSettings(userId, writeSettingsJson(defaults)));
            return defaults;
        }

        try {
            UserSettingsDTO parsed = objectMapper.readValue(settings.getSettingsJson(), UserSettingsDTO.class);
            UserSettingsDTO normalized = normalize(parsed);
            if (!writeSettingsJson(normalized).equals(settings.getSettingsJson())) {
                settings.setSettingsJson(writeSettingsJson(normalized));
                userSettingsRepository.save(settings);
            }
            return normalized;
        } catch (JsonProcessingException exception) {
            UserSettingsDTO defaults = defaultSettings();
            settings.setSettingsJson(writeSettingsJson(defaults));
            log.info("Settings saved");
            userSettingsRepository.save(settings);
            return defaults;
        }
    }

    public UserSettingsDTO saveSettingsForUser(Long userId, UserSettingsDTO dto) {
        UserSettingsDTO normalized = normalize(dto);
        UserSettings settings = userSettingsRepository.findByUserId(userId);
        if (settings == null) {
            settings = new UserSettings(userId, writeSettingsJson(normalized));
        } else {
            settings.setSettingsJson(writeSettingsJson(normalized));
        }
        log.info("Settings saved");
        userSettingsRepository.save(settings);
        return normalized;
    }

    private UserSettingsDTO normalize(UserSettingsDTO dto) {
        UserSettingsDTO defaults = defaultSettings();
        UserSettingsDTO normalized = dto != null ? dto : new UserSettingsDTO();

        ScholarHubSettingsDTO scholarHub = normalized.getScholarHub();
        if (scholarHub == null) {
            scholarHub = new ScholarHubSettingsDTO();
        }
        if (scholarHub.getPreferredGradeSystem() == null) {
            scholarHub.setPreferredGradeSystem(defaults.getScholarHub().getPreferredGradeSystem());
        }
        scholarHub.setStartOfEachLesson(normalizeLessonTimes(scholarHub.getStartOfEachLesson()));
        scholarHub.setEndOfEachLesson(normalizeLessonTimes(scholarHub.getEndOfEachLesson()));
        normalized.setScholarHub(scholarHub);

        AppearanceSettingsDTO appearance = normalized.getAppearance();
        if (appearance == null) {
            normalized.setAppearance(defaults.getAppearance());
        } else {
            if (appearance.getTheme() == null || appearance.getTheme().isBlank()) {
                appearance.setTheme(defaults.getAppearance().getTheme());
            }
            if (appearance.getDensity() == null || appearance.getDensity().isBlank()) {
                appearance.setDensity(defaults.getAppearance().getDensity());
            }
        }

        if (normalized.getNotifications() == null) {
            normalized.setNotifications(defaults.getNotifications());
        }

        return normalized;
    }

    private UserSettingsDTO defaultSettings() {
        return new UserSettingsDTO(
                new ScholarHubSettingsDTO(GradingSystemEnum.Numeric_Grading_1_to_5),
                new AppearanceSettingsDTO("system", false, "comfortable"),
                new NotificationSettingsDTO(true, true)
        );
    }

    private Map<LessonEnum, String> normalizeLessonTimes(Map<LessonEnum, String> rawLessonTimes) {
        Map<LessonEnum, String> normalized = new EnumMap<>(LessonEnum.class);
        if (rawLessonTimes == null) {
            return normalized;
        }

        for (Map.Entry<LessonEnum, String> entry : rawLessonTimes.entrySet()) {
            LessonEnum key = entry.getKey();
            String value = normalizeLessonTime(entry.getValue());
            if (key != null && value != null) {
                normalized.put(key, value);
            }
        }

        return normalized;
    }

    private String normalizeLessonTime(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return LESSON_TIME_PATTERN.matcher(normalized).matches() ? normalized : null;
    }

    private String writeSettingsJson(UserSettingsDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize user settings.", exception);
        }
    }
}
