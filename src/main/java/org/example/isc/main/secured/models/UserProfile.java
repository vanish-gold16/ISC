package org.example.isc.main.secured.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.example.isc.main.enums.CountryEnum;
import org.example.isc.main.enums.OccupationEnum;
import org.example.isc.main.enums.converter.CountryEnumConverter;
import org.example.isc.main.enums.converter.OccupationEnumConverter;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "profiles")
public class UserProfile {

    @Id
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
    private LocalDate birthDate;

    public UserProfile(User user, String bio, CountryEnum country, String city, String currentStudy, OccupationEnum occupationEnum, LocalDate birthday) {
        this.user = user;
        this.bio = bio;
        this.country = country;
        this.city = city;
        this.currentStudy = currentStudy;
        this.occupationEnum = occupationEnum;
        this.birthDate = birthday;
    }

    public UserProfile() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public CountryEnum getCountry() {
        return country;
    }

    public void setCountry(CountryEnum country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCurrentStudy() {
        return currentStudy;
    }

    public void setCurrentStudy(String currentStudy) {
        this.currentStudy = currentStudy;
    }

    public OccupationEnum getOccupationEnum() {
        return occupationEnum;
    }

    public void setOccupationEnum(OccupationEnum occupationEnum) {
        this.occupationEnum = occupationEnum;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
