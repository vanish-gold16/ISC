package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;
import org.example.isc.main.enums.scholarhub.HomeworkPriorityEnum;
import org.example.isc.main.enums.scholarhub.HomeworkStatusEnum;

import java.time.LocalDate;

@Entity
@Table(name = "homeworks")
public class Homework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "due_day_subject_id")
    private Long dueDaySubjectId;

    @Column(name = "title")
    private String title;

    @Column(name = "details")
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private HomeworkPriorityEnum priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private HomeworkStatusEnum status;

    @Column(name = "week_start")
    private LocalDate weekStart;

    public Homework() {
    }

    public Homework(Long subjectId, Long dueDaySubjectId, String title, String details, HomeworkPriorityEnum priority, HomeworkStatusEnum status, LocalDate weekStart) {
        this.subjectId = subjectId;
        this.dueDaySubjectId = dueDaySubjectId;
        this.title = title;
        this.details = details;
        this.priority = priority;
        this.status = status;
        this.weekStart = weekStart;
    }

    public Homework(String title, String details, HomeworkPriorityEnum priority, HomeworkStatusEnum status, LocalDate weekStart) {
        this.title = title;
        this.details = details;
        this.priority = priority;
        this.status = status;
        this.weekStart = weekStart;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
