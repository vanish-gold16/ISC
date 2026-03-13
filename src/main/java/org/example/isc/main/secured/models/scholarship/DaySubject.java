package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class DaySubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "day")
    private Day day;

    @OneToMany
    @JoinColumn(name = "subjects")
    private List<Subject> subjects;

    @OneToMany
    @JoinColumn(name = "homeworks")
    private List<Homework> homeworks;

    public DaySubject() {
    }

    public DaySubject(Day day, List<Subject> subjects, List<Homework> homeworks) {
        this.day = day;
        this.subjects = subjects;
        this.homeworks = homeworks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public List<Homework> getHomeworks() {
        return homeworks;
    }

    public void setHomeworks(List<Homework> homeworks) {
        this.homeworks = homeworks;
    }
}
