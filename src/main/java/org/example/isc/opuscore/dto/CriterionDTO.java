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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getDefaultScore() {
        return defaultScore;
    }

    public void setDefaultScore(int defaultScore) {
        this.defaultScore = defaultScore;
    }
}
