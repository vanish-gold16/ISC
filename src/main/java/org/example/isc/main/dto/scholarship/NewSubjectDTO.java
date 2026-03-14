package org.example.isc.main.dto.scholarship;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NewSubjectDTO {

    @NotNull
    @Size(max = 10)
    private String shortName;

    @Size(max = 70)
    private String fullName;

    @NotNull
    private Long userId;

    private Long teacherId;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
}
