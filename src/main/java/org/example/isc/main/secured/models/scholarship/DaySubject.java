package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class DaySubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "day")
    private Day day;

    @ManyToOne
    @JoinColumn(name = "subjects")
    private Subject subject;

    @OneToMany
    @JoinColumn(name = "homeworks")
    private List<Homework> homeworks;

    public DaySubject() {
    }

    public DaySubject(Day day, Subject subject, List<Homework> homeworks) {
        this.day = day;
        this.subject = subject;
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

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public List<Homework> getHomeworks() {
        return homeworks;
    }

    public void setHomeworks(List<Homework> homeworks) {
        this.homeworks = homeworks;
    }
}
