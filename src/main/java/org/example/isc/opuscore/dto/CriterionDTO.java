package org.example.isc.opuscore.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.bind.DefaultValue;

public class CriterionDTO {

    @NotNull
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private int weight;

    @NotNull
    private int defaultScore;

    public CriterionDTO(Long id, String name, String description, int weight, int defaultScore) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.defaultScore = defaultScore;
    }

    public CriterionDTO() {
    }
}
