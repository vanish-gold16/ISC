package org.example.isc.main.secured.models.scholarship;

import org.example.isc.main.enums.scholarhub.GradingSystemEnum;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConvertGradeTest {

    private final ConvertGrade convertGrade = new ConvertGrade();

    @Test
    void rejectsNumericGradeAboveUpperBound() {
        assertThrows(IllegalArgumentException.class, () ->
                convertGrade.toNormalizedScore(GradingSystemEnum.Numeric_Grading_1_to_5, "6"));
    }

    @Test
    void rejectsPercentageAboveHundred() {
        assertThrows(IllegalArgumentException.class, () ->
                convertGrade.toNormalizedScore(GradingSystemEnum.Percentage_Grading, "101"));
    }

    @Test
    void acceptsUpperBoundNumericGrade() {
        assertEquals(new BigDecimal("1.0000"),
                convertGrade.toNormalizedScore(GradingSystemEnum.Numeric_Grading_1_to_5, "5"));
    }
}
