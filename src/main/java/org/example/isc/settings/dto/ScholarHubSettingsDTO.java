package org.example.isc.settings.dto;

import org.example.isc.main.enums.scholarhub.GradingSystemEnum;

public class ScholarHubSettingsDTO {

    private GradingSystemEnum preferredGradeSystem;

    public ScholarHubSettingsDTO() {
    }

    public ScholarHubSettingsDTO(GradingSystemEnum preferredGradeSystem) {
        this.preferredGradeSystem = preferredGradeSystem;
    }

    public GradingSystemEnum getPreferredGradeSystem() {
        return preferredGradeSystem;
    }

    public void setPreferredGradeSystem(GradingSystemEnum preferredGradeSystem) {
        this.preferredGradeSystem = preferredGradeSystem;
    }
}
