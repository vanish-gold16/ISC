package org.example.isc.main.dto;

import org.example.isc.main.enums.PresenceStateEnum;

import java.time.Instant;

public record UserStatusDTO (PresenceStateEnum state, Instant lastActive) {
}
