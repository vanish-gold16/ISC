package org.example.isc.opuscore.models;

import jakarta.persistence.*;
import org.example.isc.opuscore.enums.ArtTypeEnum;
import org.example.isc.opuscore.enums.ReviewStatusEnum;

import java.util.List;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "art_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ArtTypeEnum type;

    @Column(name = "is_review")
    private Boolean isReview;

    @Column(name = "art_name",  nullable = false)
    private String artName;

    @Column(name = "art_description")
    private String artDescription;

    @Column(name = "title")
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "value", nullable = false)
    private Long value;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatusEnum status;

    @Column(name = "photo_url")
    private String photoUrl;

    public Review() {
    }

    // non review
    public Review(ArtTypeEnum type, Boolean isReview, String artName, Long value) {
        this.type = type;
        this.isReview = isReview;
        this.artName = artName;
        this.value = value;
    }

    public Review(ArtTypeEnum type, Boolean isReview, String artName, String artDescription, String title, String body, Long value) {
        this.type = type;
        this.isReview = isReview;
        this.artName = artName;
        this.artDescription = artDescription;
        this.title = title;
        this.body = body;
        this.value = value;
    }

    public ReviewStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ReviewStatusEnum status) {
        this.status = status;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ArtTypeEnum getType() {
        return type;
    }

    public void setType(ArtTypeEnum type) {
        this.type = type;
    }

    public Boolean getReview() {
        return isReview;
    }

    public void setReview(Boolean review) {
        isReview = review;
    }

    public String getArtName() {
        return artName;
    }

    public void setArtName(String artName) {
        this.artName = artName;
    }

    public String getArtDescription() {
        return artDescription;
    }

    public void setArtDescription(String artDescription) {
        this.artDescription = artDescription;
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
