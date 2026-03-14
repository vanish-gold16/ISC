package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;
import org.example.isc.main.enums.HomeworkStatusEnum;

import java.util.List;

@Entity
@Table(name = "homeworks")
public class Homework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject")
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private HomeworkStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_subject")
    private DaySubject daySubject;

    public Homework() {
    }

    public Homework(Subject subject, HomeworkStatusEnum status) {
        this.subject = subject;
        this.status = status;
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
}
