package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class DaySubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day")
    private Day day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject")
    private Subject subject;

    @OneToMany(mappedBy = "assignedDaySubject", fetch = FetchType.LAZY)
    private List<Grade> grades;

    @OneToMany(mappedBy = "dueDaySubject", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Homework> homeworks = new ArrayList<>();

    @Column(name = "lesson_order")
    private Long lessonOrder;

    @Column(name = "room")
    private String room;

    public DaySubject() {
    }

    public DaySubject(Day day, Subject subject, Long lessonOrder) {
        this.day = day;
        this.subject = subject;
        this.lessonOrder = lessonOrder;
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

    public Long getLessonOrder() {
        return lessonOrder;
    }

    public void setLessonOrder(Long lessonOrder) {
        this.lessonOrder = lessonOrder;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public List<Grade> getGrades() {
        return grades;
    }

    public void setGrades(List<Grade> grades) {
        this.grades = grades;
    }

    public List<Homework> getHomeworks() {
        return homeworks;
    }

    public void setHomeworks(List<Homework> homeworks) {
        this.homeworks = homeworks;
    }
}
