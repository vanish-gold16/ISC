package org.example.isc.main.dto;

import org.example.isc.main.enums.PresenceState;

import java.time.Instant;

public record UserStatusDTO (PresenceState presenceState, Instant lastActive) {
}
