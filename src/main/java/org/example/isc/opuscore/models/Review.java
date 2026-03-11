package org.example.isc.opuscore.models;

import jakarta.persistence.*;
import org.example.isc.opuscore.enums.ArtTypeEnum;

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

}
