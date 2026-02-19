package org.example.isc.main.secured.models;

import jakarta.persistence.*;
import org.example.isc.main.enums.OccupationEnum;
import org.example.isc.main.enums.RoleEnumConverter;

@Entity
@Table(name = "profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "bio")
    private String bio;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    @Column(name = "current_study")
    private String currentStudy;

    @Column(name = "occupation")
    @Convert(converter = OccupationEnum.class)
    private OccupationEnum occupationEnum;



}
