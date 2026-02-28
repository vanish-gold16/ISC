package org.example.isc.main.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.isc.main.secured.models.User;

import java.awt.*;
import java.time.LocalDateTime;

public class NewPostForm {

    @NotBlank
    private User user;

    @Size(max = 50)
    private String title;

    @Size(max = 200)
    private String body;

    @NotBlank
    private LocalDateTime postTime;

    private Image photo;

    public NewPostForm() {
    }

    public NewPostForm(User user, String title, String body, LocalDateTime postTime, Image photo) {
        this.user = user;
        this.title = title;
        this.body = body;
        this.postTime = postTime;
        this.photo = photo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public LocalDateTime getPostTime() {
        return postTime;
    }

    public void setPostTime(LocalDateTime postTime) {
        this.postTime = postTime;
    }

    public Image getPhoto() {
        return photo;
    }

    public void setPhoto(Image photo) {
        this.photo = photo;
    }
}
