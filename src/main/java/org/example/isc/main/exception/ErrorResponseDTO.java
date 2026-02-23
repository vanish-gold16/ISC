package org.example.isc.main.exception;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        String message, String detailedMessage, LocalDateTime time
) {
}
