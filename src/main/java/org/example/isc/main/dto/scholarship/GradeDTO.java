package org.example.isc.main.dto.scholarship;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.isc.main.enums.scholarhub.GradeReasonEnum;
import org.example.isc.main.enums.scholarhub.GradingSystemEnum;

import java.math.BigDecimal;

public class GradeDTO {

    @NotNull
    private Long subjectId;

    private Long assignedDaySubjectId;

    @NotNull
    private GradingSystemEnum system;

    @NotNull
    private GradeReasonEnum reason;

    private String description;

    @NotBlank
    private String value;

    private BigDecimal converted;

    public GradeDTO() {
    }

    public GradeDTO(Long subjectId, Long assignedDaySubjectId, GradingSystemEnum system, GradeReasonEnum reason, String description, String value, BigDecimal converted) {
        this.subjectId = subjectId;
        this.assignedDaySubjectId = assignedDaySubjectId;
        this.system = system;
        this.reason = reason;
        this.description = description;
        this.value = value;
        this.converted = converted;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Long getAssignedDaySubjectId() {
        return assignedDaySubjectId;
    }

    public void setAssignedDaySubjectId(Long assignedDaySubjectId) {
        this.assignedDaySubjectId = assignedDaySubjectId;
    }

    public GradingSystemEnum getSystem() {
        return system;
    }

    public void setSystem(GradingSystemEnum system) {
        this.system = system;
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

    public GradeReasonEnum getReason() {
        return reason;
    }

    public void setReason(GradeReasonEnum reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
