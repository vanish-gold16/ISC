package org.example.isc.opuscore.dto.art;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.isc.opuscore.enums.ArtTypeEnum;

import java.time.LocalDateTime;

public class ArtDTO {

    @NotNull
    private Long artId;

    @NotNull
    private ArtTypeEnum type;

    @NotNull
    private String name;

    @NotNull
    private String author;

    @NotNull
    @Size(max = 1000)
    private String description;

    @NotNull
    private String coverUrl;

    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime editedAt;

    public ArtDTO() {
    }

    public ArtDTO(Long artId, ArtTypeEnum type, String name, String author, String description, LocalDateTime createdAt) {
        this.artId = artId;
        this.type = type;
        this.name = name;
        this.author = author;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getArtId() {
        return artId;
    }

    public void setArtId(Long artId) {
        this.artId = artId;
    }

    public ArtTypeEnum getType() {
        return type;
    }

    public void setType(ArtTypeEnum type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }
}
