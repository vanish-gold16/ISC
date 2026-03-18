package org.example.isc.main.dto.scholarship;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.isc.main.enums.HomeworkPriorityEnum;
import org.example.isc.main.enums.HomeworkStatusEnum;

import java.time.LocalDate;

public class HomeworkDTO {

    private Long id;

    @NotNull
    @Size(max = 30)
    private String title;

    @Size(max = 500)
    private String details;

    @NotNull
    private HomeworkPriorityEnum priority;

    @NotNull
    private Long daySubjectId;

    @NotNull
    private HomeworkStatusEnum status;

    @NotNull
    private LocalDate weekStart;

    public HomeworkDTO() {
    }

    public HomeworkDTO(Long id, String title, String details, HomeworkPriorityEnum priority, Long daySubjectId, HomeworkStatusEnum status, LocalDate weekStart) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.priority = priority;
        this.daySubjectId = daySubjectId;
        this.status = status;
        this.weekStart = weekStart;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public HomeworkPriorityEnum getPriority() {
        return priority;
    }

    public void setPriority(HomeworkPriorityEnum priority) {
        this.priority = priority;
    }

    public Long getDaySubjectId() {
        return daySubjectId;
    }

    public void setDaySubjectId(Long daySubjectId) {
        this.daySubjectId = daySubjectId;
    }

    public HomeworkStatusEnum getStatus() {
        return status;
    }

    public void setStatus(HomeworkStatusEnum status) {
        this.status = status;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }
}
