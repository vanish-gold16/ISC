package org.example.isc.opuscore.dto;

import jakarta.validation.constraints.NotNull;
import org.example.isc.opuscore.enums.ArtTypeEnum;
import org.example.isc.opuscore.enums.ReviewStatusEnum;

import java.time.LocalDateTime;

public class AdminArtAnswerDTO {

    @NotNull
    private Long id;

    @NotNull
    private Long requesterId;

    @NotNull
    private ArtTypeEnum type;

    @NotNull
    private String name;

    @NotNull
    private String author;

    @NotNull
    private String description;

    @NotNull
    private String imageUrl;

    @NotNull
    private ReviewStatusEnum status;

    private String adminNote;

    private String rejectionReason;

    private Long approvedArtworkId;

    @NotNull
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public AdminArtAnswerDTO() {
    }

    public AdminArtAnswerDTO(Long requesterId, ArtTypeEnum type, String name, String author, String description, String imageUrl, ReviewStatusEnum status, LocalDateTime createdAt) {
        this.requesterId = requesterId;
        this.type = type;
        this.name = name;
        this.author = author;
        this.description = description;
        this.imageUrl = imageUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ReviewStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ReviewStatusEnum status) {
        this.status = status;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Long getApprovedArtworkId() {
        return approvedArtworkId;
    }

    public void setApprovedArtworkId(Long approvedArtworkId) {
        this.approvedArtworkId = approvedArtworkId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
