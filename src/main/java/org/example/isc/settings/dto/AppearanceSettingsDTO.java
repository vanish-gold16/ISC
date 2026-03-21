package org.example.isc.settings.dto;

import org.example.isc.settings.enums.ThemeEnum;

public class AppearanceSettingsDTO {

    private ThemeEnum theme;

    private boolean reduceMotion;

    public AppearanceSettingsDTO(ThemeEnum theme, boolean reduceMotion) {
        this.theme = theme;
        this.reduceMotion = reduceMotion;
    }

    public ThemeEnum getTheme() {
        return theme;
    }

    public void setTheme(ThemeEnum theme) {
        this.theme = theme;
    }

    public boolean isReduceMotion() {
        return reduceMotion;
    }

    public void setReduceMotion(boolean reduceMotion) {
        this.reduceMotion = reduceMotion;
    }
}
