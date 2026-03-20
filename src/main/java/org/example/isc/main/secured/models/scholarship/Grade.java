package org.example.isc.main.secured.models.scholarship;

import jakarta.persistence.*;
import org.example.isc.main.enums.scholarhub.GradingSystemEnum;

import java.math.BigDecimal;

@Entity
@Table(name = "grades")
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subject", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "assigned_day_subject")
    private DaySubject assignedDaySubject;

    @Enumerated(EnumType.STRING)
    @Column(name = "grading_system", nullable = false)
    private GradingSystemEnum gradingSystem;

    @Column(name = "value", nullable = false)
    private String value;

    @Column(name = "int_value", precision = 5, scale = 4, nullable = false)
    private BigDecimal converted;

    public Grade() {
    }

    public Grade(Subject subject, DaySubject assignedDaySubject, GradingSystemEnum gradingSystem, String value, BigDecimal converted) {
        this.subject = subject;
        this.assignedDaySubject = assignedDaySubject;
        this.gradingSystem = gradingSystem;
        this.value = value;
        this.converted = converted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public DaySubject getAssignedDaySubject() {
        return assignedDaySubject;
    }

    public void setAssignedDaySubject(DaySubject assignedDaySubject) {
        this.assignedDaySubject = assignedDaySubject;
    }

    public GradingSystemEnum getGradingSystem() {
        return gradingSystem;
    }

    public void setGradingSystem(GradingSystemEnum gradingSystem) {
        this.gradingSystem = gradingSystem;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public BigDecimal getConverted() {
        return converted;
    }

    public void setConverted(BigDecimal converted) {
        this.converted = converted;
    }
}
