package org.example.isc.main.secured.models;

import jakarta.persistence.*;
import org.example.isc.main.enums.OccupationEnum;
import org.example.isc.main.enums.OccupationEnumConverter;
import org.example.isc.main.enums.RoleEnumConverter;

import java.util.Date;

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
    @Convert(converter = OccupationEnumConverter.class)
    private OccupationEnum occupationEnum;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "cover_url")
    private String coverUrl;

    @Column(name = "birth_date")
    private Date birthDate;

}
