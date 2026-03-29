package org.example.isc.opuscore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import org.example.isc.opuscore.enums.ArtTypeEnum;
import org.example.isc.opuscore.models.ReviewCriterion;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class NewReviewDTO {

    @NotNull
    private ArtTypeEnum artType;

    private Long artRequestId;

    @NotNull
    private boolean isReview;

    private Long artworkId;

    @Size(max = 50)
    private String title;

    @Size(max = 2000)
    private String body;

    @NotNull
    @PositiveOrZero
    @Max(100)
    private Long value;

    private MultipartFile image;
    private String imageUrl;

    @NotNull
    private List<ReviewCriterion> criteria;

    public List<ReviewCriterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<ReviewCriterion> criteria) {
        this.criteria = criteria;
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

    public NewReviewDTO() {
    }

    public NewReviewDTO(ArtTypeEnum artType, boolean isReview, Long artworkId, String title, String body, Long value) {
        this.artType = artType;
        this.isReview = isReview;
        this.artworkId = artworkId;
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

    public Long getArtworkId() {
        return artworkId;
    }

    public void setArtworkId(Long artworkId) {
        this.artworkId = artworkId;
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

    public Long getArtRequestId() {
        return artRequestId;
    }

    public void setArtRequestId(Long artRequestId) {
        this.artRequestId = artRequestId;
    }
}
