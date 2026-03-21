package org.example.isc.settings.repository;

import org.example.isc.settings.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    UserSettings findByUserId(Long userId);
}
