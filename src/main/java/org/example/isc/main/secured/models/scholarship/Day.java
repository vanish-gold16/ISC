package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.util.List;

@Entity
@Table(name = "days")
public class Day {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule")
    private Schedule schedule;

    @OneToMany
    @JoinColumn(name = "subjects")
    private List<Subject> subjects;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_enum")
    private DayOfWeek dayOfWeek;

    @OneToOne
    @JoinColumn(name = "day_subject")
    private DaySubject daySubject;

    public Day() {
    }

    public Day(Schedule schedule, List<Subject> subjects, DayOfWeek dayOfWeek) {
        this.schedule = schedule;
        this.subjects = subjects;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public DaySubject getDaySubject() {
        return daySubject;
    }

    public void setDaySubject(DaySubject daySubject) {
        this.daySubject = daySubject;
    }
}
