package org.example.isc.opuscore.dto;

public class ReviewCriterionViewDTO {

    private final String name;
    private final String description;
    private final int score;
    private final int weight;

    public ReviewCriterionViewDTO(String name, String description, int score, int weight) {
        this.name = name;
        this.description = description;
        this.score = score;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getScore() {
        return score;
    }

    public int getWeight() {
        return weight;
    }
}
