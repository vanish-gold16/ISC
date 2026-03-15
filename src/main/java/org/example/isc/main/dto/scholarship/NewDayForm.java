package org.example.isc.main.dto.scholarship;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewDayForm {

    @NotNull
    private DayOfWeek dayOfWeek;

    private List<NewLessonRequest> lessons = new ArrayList<>();

    public NewDayForm() {
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public List<NewLessonRequest> getLessons() {
        return lessons;
    }

    public void setLessons(List<NewLessonRequest> lessons) {
        this.lessons = lessons;
    }
}
