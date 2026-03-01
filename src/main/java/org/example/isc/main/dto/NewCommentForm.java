package org.example.isc.main.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NewCommentForm {

    @NotNull
    @Size(max = 100)
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
