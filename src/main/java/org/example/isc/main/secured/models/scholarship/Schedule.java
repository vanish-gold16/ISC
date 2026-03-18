package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.example.isc.main.secured.models.users.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 32)
    private List<Day> days = new ArrayList<>();

    // subject - day - schedule - class - school

    public Schedule() {
    }

    public Schedule(User user, List<Day> days) {
        this.user = user;
        setDays(days);
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
        this.days.clear();
        if (days != null) {
            this.days.addAll(days);
        }
    }
}
