package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;

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
}
