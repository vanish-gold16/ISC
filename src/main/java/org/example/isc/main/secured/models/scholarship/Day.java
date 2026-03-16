package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.time.DayOfWeek;
import java.util.List;

@Entity
@Table(name = "days")
public class Day {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule")
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_enum")
    private DayOfWeek dayOfWeek;

    @OneToMany(mappedBy = "day", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 32)
    private List<DaySubject> lessons;

    public Day() {
    }

    public Day(Schedule schedule, DayOfWeek dayOfWeek, List<DaySubject> lessons) {
        this.schedule = schedule;
        this.dayOfWeek = dayOfWeek;
        this.lessons = lessons;
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

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public List<DaySubject> getLessons() {
        return lessons;
    }

    public void setLessons(List<DaySubject> lessons) {
        this.lessons = lessons;
    }
}
