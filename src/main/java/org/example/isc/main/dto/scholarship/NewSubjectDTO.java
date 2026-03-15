package org.example.isc.main.dto.scholarship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class NewSubjectDTO {

    @NotBlank
    @Size(max = 70)
    private String fullName;

    @Size(max = 12)
    private String shortName;

    @Size(max = 80)
    private String teacherName;

    @Size(max = 40)
    private String room;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }
}
