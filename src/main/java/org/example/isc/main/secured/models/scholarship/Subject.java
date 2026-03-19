package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.example.isc.main.secured.models.users.User;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_name", nullable = false)
    private String shortName;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "room")
    private String room;

    @Column(name = "color")
    @Size(max = 7)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "subject_teachers",
            joinColumns = @JoinColumn(name = "subject_id"),
            inverseJoinColumns = @JoinColumn(name = "teacher_id")
    )
    @BatchSize(size = 32)
    private List<Teacher> teachers;

    public Subject() {
    }

    public Subject(String shortName, String fullName, String room, String color, User user, List<Teacher> teachers) {
        setFullName(fullName);
        setShortName(shortName);
        this.room = room;
        this.color = color;
        this.user = user;
        setTeachers(teachers);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName != null && !shortName.isBlank() ? shortName : buildAutoShortName(getFullName());
    }

    public void setShortName(String shortName) {
        this.shortName = shortName == null || shortName.isBlank() ? null : shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        this.teachers = teachers == null ? new ArrayList<>() : new ArrayList<>(teachers);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @PrePersist
    @PreUpdate
    void syncNames() {
        if ((shortName == null || shortName.isBlank()) && fullName != null && !fullName.isBlank()) {
            shortName = buildAutoShortName(fullName);
        }
    }

    private String buildAutoShortName(String value) {
        String[] words = value.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String word : words) {
            if (!word.isBlank() && Character.isLetterOrDigit(word.charAt(0))) {
                initials.append(Character.toUpperCase(word.charAt(0)));
            }
        }

        if (!initials.isEmpty()) {
            return initials.length() <= 12 ? initials.toString() : initials.substring(0, 12);
        }

        return value.length() <= 12 ? value : value.substring(0, 12);
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }


}
