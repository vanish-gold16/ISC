package org.example.isc.main.common.dto;

import jakarta.validation.constraints.Size;
import org.example.isc.main.enums.CountryEnum;
import org.example.isc.main.enums.OccupationEnum;

import java.util.Date;

public class ProfileSetupForm {

    @Size(max = 300)
    private String bio;

    private CountryEnum country;

    @Size(max = 80)
    private String city;

    @Size(max = 120)
    private String currentStudy;

    private OccupationEnum occupationEnum;

    private String avatarUrl;

    private String coverUrl;

    private Date birthDate;

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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}
