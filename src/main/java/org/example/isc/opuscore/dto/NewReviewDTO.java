package org.example.isc.opuscore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.isc.opuscore.enums.ArtTypeEnum;
import org.example.isc.opuscore.enums.ReviewStatusEnum;
import org.springframework.web.multipart.MultipartFile;

public class NewReviewDTO {

    @NotNull
    private ArtTypeEnum artType;

    @NotNull
    private boolean isReview;

    @NotNull
    @Size(min =  1, max = 100)
    private String name;

    @Size(min = 1, max = 300)
    private String description;

    @Size(max = 50)
    private String title;

    @Size(max = 2000)
    private String body;

    @NotNull
    @Size(max = 100)
    private Long value;

    private MultipartFile image;

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public NewReviewDTO() {
    }

    public NewReviewDTO(ArtTypeEnum artType, boolean isReview, String name, String description, String title, String body, Long value) {
        this.artType = artType;
        this.isReview = isReview;
        this.name = name;
        this.description = description;
        this.title = title;
        this.body = body;
        this.value = value;
    }

    public ArtTypeEnum getArtType() {
        return artType;
    }

    public void setArtType(ArtTypeEnum artType) {
        this.artType = artType;
    }

    public boolean isReview() {
        return isReview;
    }

    public void setReview(boolean review) {
        isReview = review;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
