package org.example.isc.main.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.isc.main.enums.CountryEnum;
import org.example.isc.main.enums.OccupationEnum;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class EditRequest {

    @NotBlank
    @Size(min = 2, max = 50)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 50)
    private String lastName;

    @NotBlank
    @Size(min = 4, max = 20)
    private String username;

    @NotBlank
    @Email
    private String email;

    @Size(max = 300)
    private String bio;

    private CountryEnum country;

    @Size(max = 80)
    private String city;

    @Size(max = 120)
    private String currentStudy;

    private OccupationEnum occupationEnum;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date birthDate;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}
