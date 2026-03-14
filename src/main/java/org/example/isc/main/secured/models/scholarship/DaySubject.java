package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;

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

    @Column(name = "order")
    private Long order;

    @OneToMany(mappedBy = "daySubject", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Homework> homeworks;

    @Column(name = "room")
    private Long roomId;

    public DaySubject() {
    }

    public DaySubject(Day day, Subject subject, Long order, List<Homework> homeworks) {
        this.day = day;
        this.subject = subject;
        this.order = order;
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

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }
}
