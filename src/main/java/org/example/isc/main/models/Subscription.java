package org.example.isc.main.models;

import jakarta.persistence.*;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id")
    @OneToOne
    private User follower;

    @Column(name = "followed_id")
    @OneToOne
    private User followed;

}
