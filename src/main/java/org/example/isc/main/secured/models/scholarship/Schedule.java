package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;
import org.example.isc.main.secured.models.users.User;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user")
    private User user;

    @OneToMany
    @JoinColumn(name = "days")
    private List<Day> days;

    // subject - day - schedule - class - school

    public Schedule() {
    }

    public Schedule(User user, List<Day> days) {
        this.days = days;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Day> getDays() {
        return days;
    }

    public void setDays(List<Day> days) {
        this.days = days;
    }
}
