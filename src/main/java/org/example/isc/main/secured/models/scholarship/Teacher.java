package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @ManyToMany(mappedBy = "teachers")
    private List<Subject> subjects;

    @Column(name = "age")
    private int age;

    public Teacher() {
    }

    public Teacher(String fullName, List<Subject> subjects, int age) {
        this.fullName = fullName;
        this.subjects = subjects;
        this.age = age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
