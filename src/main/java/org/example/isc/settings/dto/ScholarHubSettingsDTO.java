package org.example.isc.settings.dto;

import org.example.isc.main.enums.scholarhub.GradingSystemEnum;
import org.example.isc.settings.enums.LessonEnum;

import java.util.HashMap;
import java.util.Map;

public class ScholarHubSettingsDTO {

    private GradingSystemEnum preferredGradeSystem;
    private Map<LessonEnum, String> startOfEachLesson;
    private Map<LessonEnum, String> endOfEachLesson;

    public ScholarHubSettingsDTO() {
    }

    public ScholarHubSettingsDTO(GradingSystemEnum preferredGradeSystem) {
        this.preferredGradeSystem = preferredGradeSystem;
        this.startOfEachLesson = new HashMap<>();
        this.endOfEachLesson = new HashMap<>();
    }

    public ScholarHubSettingsDTO(GradingSystemEnum preferredGradeSystem, Map<LessonEnum, String> startOfEachLesson) {
        this.preferredGradeSystem = preferredGradeSystem;
        this.startOfEachLesson = startOfEachLesson;
        this.endOfEachLesson = new HashMap<>();
    }

    public ScholarHubSettingsDTO(
            GradingSystemEnum preferredGradeSystem,
            Map<LessonEnum, String> startOfEachLesson,
            Map<LessonEnum, String> endOfEachLesson
    ) {
        this.preferredGradeSystem = preferredGradeSystem;
        this.startOfEachLesson = startOfEachLesson;
        this.endOfEachLesson = endOfEachLesson;
    }

    public GradingSystemEnum getPreferredGradeSystem() {
        return preferredGradeSystem;
    }

    public void setPreferredGradeSystem(GradingSystemEnum preferredGradeSystem) {
        this.preferredGradeSystem = preferredGradeSystem;
    }

    public Map<LessonEnum, String> getStartOfEachLesson() {
        return startOfEachLesson;
    }

    public void setStartOfEachLesson(Map<LessonEnum, String> startOfEachLesson) {
        this.startOfEachLesson = startOfEachLesson;
    }

    public Map<LessonEnum, String> getEndOfEachLesson() {
        return endOfEachLesson;
    }

    public void setEndOfEachLesson(Map<LessonEnum, String> endOfEachLesson) {
        this.endOfEachLesson = endOfEachLesson;
    }
}
