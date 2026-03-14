package org.example.isc.main.dto;

import jakarta.validation.constraints.NotNull;
import org.example.isc.main.secured.models.scholarship.Schedule;
import org.example.isc.main.secured.models.scholarship.Subject;

import java.time.DayOfWeek;
import java.util.List;

public class NewDayForm {

    @NotNull
    private Long scheduleId;

    private List<Long> subjectIds;

    @NotNull
    private DayOfWeek dayOfWeek;

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public List<Long> getSubjectIds() {
        return subjectIds;
    }

    public void setSubjectIds(List<Long> subjectIds) {
        this.subjectIds = subjectIds;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
