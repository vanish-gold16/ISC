package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;
import org.example.isc.main.enums.HomeworkPriorityEnum;
import org.example.isc.main.enums.HomeworkStatusEnum;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;

@Entity
@Table(name = "homeworks")
public class Homework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "details")
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private HomeworkPriorityEnum priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject")
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private HomeworkStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_subject")
    private DaySubject daySubject;

    @Column(name = "week_start")
    private LocalDate weekStart;

    public Homework() {
    }

    public Homework(String title, String details, HomeworkPriorityEnum priority, Subject subject, HomeworkStatusEnum status, LocalDate weekStart) {
        this.title = title;
        this.details = details;
        this.priority = priority;
        this.subject = subject;
        this.status = status;
        this.weekStart = weekStart;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public HomeworkStatusEnum getStatus() {
        return status;
    }

    public void setStatus(HomeworkStatusEnum status) {
        this.status = status;
    }

    public DaySubject getDaySubject() {
        return daySubject;
    }

    public void setDaySubject(DaySubject daySubject) {
        this.daySubject = daySubject;
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
}
