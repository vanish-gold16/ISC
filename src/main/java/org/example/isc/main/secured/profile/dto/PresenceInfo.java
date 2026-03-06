package org.example.isc.main.secured.profile.dto;

import java.time.LocalDateTime;

public class PresenceInfo {

    private Long userId;
    private boolean online;
    private LocalDateTime lastSeenAt;

    public PresenceInfo() {
    }

    public PresenceInfo(Long userId, boolean online, LocalDateTime lastSeenAt) {
        this.userId = userId;
        this.online = online;
        this.lastSeenAt = lastSeenAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
