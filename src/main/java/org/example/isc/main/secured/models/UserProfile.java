package org.example.isc.main.secured.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.example.isc.main.enums.CountryEnum;
import org.example.isc.main.enums.OccupationEnum;
import org.example.isc.main.enums.converter.CountryEnumConverter;
import org.example.isc.main.enums.converter.OccupationEnumConverter;

import java.util.Date;

@Entity
@Table(name = "profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "bio", length = 300)
    @Size(max = 300)
    private String bio;

    @Column(name = "country", length = 80)
    @Convert(converter = CountryEnumConverter.class)
    private CountryEnum country;

    @Column(name = "city", length = 80)
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
