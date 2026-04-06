package org.example.isc.opuscore.dto.art;

import org.example.isc.opuscore.enums.ArtTypeEnum;

public record ArtDropdownDTO(
        Long id,
        String name,
        String author,
        String description,
        ArtTypeEnum type,
        String coverUrl
) {
}
