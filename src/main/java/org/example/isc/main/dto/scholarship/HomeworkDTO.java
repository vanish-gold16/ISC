package org.example.isc.main.dto.scholarship;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.isc.main.enums.scholarhub.HomeworkPriorityEnum;
import org.example.isc.main.enums.scholarhub.HomeworkStatusEnum;
import org.example.isc.main.enums.scholarhub.GradingSystemEnum;

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
    private Long subjectId;

    @NotNull
    private Long dueDaySubjectId;

    @NotNull
    private HomeworkStatusEnum status;

    @NotNull
    private LocalDate weekStart;

    private Long gradeId;

    private GradingSystemEnum gradeSystem;

    @Size(max = 100)
    private String gradeValue;


    public HomeworkDTO(Long id, String title, String details, HomeworkPriorityEnum priority, Long subjectId, Long dueDaySubjectId, HomeworkStatusEnum status, LocalDate weekStart, Long gradeId, GradingSystemEnum gradeSystem, String gradeValue) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.priority = priority;
        this.subjectId = subjectId;
        this.dueDaySubjectId = dueDaySubjectId;
        this.status = status;
        this.weekStart = weekStart;
        this.gradeId = gradeId;
        this.gradeSystem = gradeSystem;
        this.gradeValue = gradeValue;
    }

    public HomeworkDTO() {
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

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getDueDaySubjectId() {
        return dueDaySubjectId;
    }

    public void setDueDaySubjectId(Long dueDaySubjectId) {
        this.dueDaySubjectId = dueDaySubjectId;
    }

    public Long getGradeId() {
        return gradeId;
    }

    public void setGradeId(Long gradeId) {
        this.gradeId = gradeId;
    }

    public GradingSystemEnum getGradeSystem() {
        return gradeSystem;
    }

    public void setGradeSystem(GradingSystemEnum gradeSystem) {
        this.gradeSystem = gradeSystem;
    }

    public String getGradeValue() {
        return gradeValue;
    }

    public void setGradeValue(String gradeValue) {
        this.gradeValue = gradeValue;
    }
}
