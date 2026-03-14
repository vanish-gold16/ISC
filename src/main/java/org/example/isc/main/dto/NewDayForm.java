package org.example.isc.main.dto;

import jakarta.validation.constraints.NotNull;
import org.example.isc.main.secured.models.scholarship.Schedule;
import org.example.isc.main.secured.models.scholarship.Subject;

import java.time.DayOfWeek;
import java.util.List;

public class NewDayForm {

    @NotNull
    private Schedule schedule;

    private List<Subject> subjects;

    @NotNull
    private DayOfWeek dayOfWeek;

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
