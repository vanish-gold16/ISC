package org.example.isc.settings.dto;

import org.example.isc.main.enums.scholarhub.GradingSystemEnum;

public class ScholarHubSettingsDTO {

    private GradingSystemEnum preferredGradingSystem;

    public ScholarHubSettingsDTO(GradingSystemEnum preferredGradingSystem) {
        this.preferredGradingSystem = preferredGradingSystem;
    }

    public GradingSystemEnum getPreferredGradingSystem() {
        return preferredGradingSystem;
    }

    public void setPreferredGradingSystem(GradingSystemEnum preferredGradingSystem) {
        this.preferredGradingSystem = preferredGradingSystem;
    }
}
