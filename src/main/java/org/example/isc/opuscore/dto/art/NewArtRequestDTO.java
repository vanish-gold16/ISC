package org.example.isc.opuscore.dto.art;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.isc.opuscore.enums.ArtTypeEnum;
import org.springframework.web.multipart.MultipartFile;

public class NewArtRequestDTO {

    private Long requesterId;

    @NotNull
    private ArtTypeEnum type;

    @NotNull
    @Size(max = 100)
    private String name;

    @NotNull
    @Size(max = 50)
    private String author;

    @Size(max = 1000)
    private String description;

    private MultipartFile image;
    private String imageUrl;

    public NewArtRequestDTO() {
    }

    public NewArtRequestDTO(Long requesterId, ArtTypeEnum type, String name, String author, String description, String imageUrl) {
        this.requesterId = requesterId;
        this.type = type;
        this.name = name;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
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

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
