package org.example.isc.settings.dto;

public class NotificationSettingsDTO {

    private boolean desktop;
    private boolean sound;

    public NotificationSettingsDTO() {
    }

    public NotificationSettingsDTO(boolean desktop, boolean sound) {
        this.desktop = desktop;
        this.sound = sound;
    }

    public boolean isDesktop() {
        return desktop;
    }

    public void setDesktop(boolean desktop) {
        this.desktop = desktop;
    }

    public boolean isSound() {
        return sound;
    }

    public void setSound(boolean sound) {
        this.sound = sound;
    }
}
