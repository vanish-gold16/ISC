package org.example.isc.main.dto;

import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class NewPostForm {

    @Size(max = 50)
    private String title;

    @Size(max = 200)
    private String body;

    private MultipartFile photo;

    public NewPostForm() {
    }

    public NewPostForm(String title, String body, MultipartFile photo) {
        this.title = title;
        this.body = body;
        this.photo = photo;
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

    public MultipartFile getPhoto() {
        return photo;
    }

    public void setPhoto(MultipartFile photo) {
        this.photo = photo;
    }
}