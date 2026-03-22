package org.example.isc.settings.dto;

public class AppearanceSettingsDTO {

    private String theme;
    private boolean reduceMotion;
    private String density;

    public AppearanceSettingsDTO() {
    }

    public AppearanceSettingsDTO(String theme, boolean reduceMotion, String density) {
        this.theme = theme;
        this.reduceMotion = reduceMotion;
        this.density = density;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public boolean isReduceMotion() {
        return reduceMotion;
    }

    public void setReduceMotion(boolean reduceMotion) {
        this.reduceMotion = reduceMotion;
    }

    public String getDensity() {
        return density;
    }

    public void setDensity(String density) {
        this.density = density;
    }
}
