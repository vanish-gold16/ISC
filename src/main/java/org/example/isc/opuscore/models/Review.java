package org.example.isc.opuscore.models;

import jakarta.persistence.*;
import org.example.isc.main.secured.models.users.User;
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

    @ManyToOne
    @JoinColumn(name = "artwork")
    private Artwork artwork;

    @Column(name = "art_name", nullable = false)
    private String artName;

    @Column(name = "art_author", length = 120)
    private String artAuthor;

    @Column(name = "art_description", length = 1000)
    private String artDescription;

    @Column(name = "title")
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "value", nullable = false)
    private int value;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReviewStatusEnum status = ReviewStatusEnum.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "photo_url", length = 1000)
    private String photoUrl;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewCriterion> criteriaScores;

    public Review() {
    }

    // non review
    public Review(ArtTypeEnum type, Boolean isReview, Artwork artwork, List<ReviewCriterion> criteriaScores) {
        this.type = type;
        this.isReview = isReview;
        setArtwork(artwork);
        this.criteriaScores = criteriaScores;
    }

    public Review(
            ArtTypeEnum type,
            Boolean isReview,
            Artwork artwork,
            String title,
            String body,
            List<ReviewCriterion> criteriaScores
    ) {
        this.type = type;
        this.isReview = isReview;
        setArtwork(artwork);
        this.title = title;
        this.body = body;
        this.criteriaScores = criteriaScores;
    }

    @PrePersist
    @PreUpdate
    private void syncArtworkSnapshot() {
        if (artwork == null) {
            return;
        }

        artName = artwork.getName();
        artAuthor = artwork.getAuthor();
        artDescription = artwork.getDescription();
        if (type == null) {
            type = artwork.getType();
        }
    }

    public List<ReviewCriterion> getCriteriaScores() {
        return criteriaScores;
    }

    public void setCriteriaScores(List<ReviewCriterion> criteriaScores) {
        this.criteriaScores = criteriaScores;
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

    public String getDisplayPhotoUrl() {
        String reviewPhoto = blankToNull(photoUrl);
        if (reviewPhoto != null) {
            return reviewPhoto;
        }
        return artwork != null ? blankToNull(artwork.getCoverUrl()) : null;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
        syncArtworkSnapshot();
    }

    public String getArtName() {
        return artName != null ? artName : (artwork != null ? artwork.getName() : null);
    }

    public void setArtName(String artName) {
        this.artName = artName;
    }

    public String getArtAuthor() {
        return artAuthor != null ? artAuthor : (artwork != null ? artwork.getAuthor() : null);
    }

    public void setArtAuthor(String artAuthor) {
        this.artAuthor = artAuthor;
    }

    public String getArtDescription() {
        return artDescription != null ? artDescription : (artwork != null ? artwork.getDescription() : null);
    }

    public void setArtDescription(String artDescription) {
        this.artDescription = artDescription;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
