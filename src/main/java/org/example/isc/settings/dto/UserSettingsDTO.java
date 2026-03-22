package org.example.isc.settings.dto;

public class UserSettingsDTO {

    private ScholarHubSettingsDTO scholarHub;

    private AppearanceSettingsDTO appearance;

    private NotificationSettingsDTO notifications;

    public UserSettingsDTO() {
    }

    public UserSettingsDTO(ScholarHubSettingsDTO scholarHub, AppearanceSettingsDTO appearance, NotificationSettingsDTO notifications) {
        this.scholarHub = scholarHub;
        this.appearance = appearance;
        this.notifications = notifications;
    }

    public ScholarHubSettingsDTO getScholarHub() {
        return scholarHub;
    }

    public void setScholarHub(ScholarHubSettingsDTO scholarHub) {
        this.scholarHub = scholarHub;
    }

    public AppearanceSettingsDTO getAppearance() {
        return appearance;
    }

    public void setAppearance(AppearanceSettingsDTO appearance) {
        this.appearance = appearance;
    }

    public NotificationSettingsDTO getNotifications() {
        return notifications;
    }

    public void setNotifications(NotificationSettingsDTO notifications) {
        this.notifications = notifications;
    }
}
