package org.example.isc.opuscore.models;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



}
