package org.example.isc.main.secured.models.scholarship;

import org.example.isc.main.enums.scholarhub.GradingSystemEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ConvertGrade {

    private BigDecimal converted;
    private String value;

    public ConvertGrade(String value) {
        this.value = value;
    }

    public BigDecimal toNormalizedScore(GradingSystemEnum system, String value){
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty value");
        }

        String cleanValue = value.trim().toUpperCase();

        switch (system){
            case Percentage_Grading:
                converted = new BigDecimal(cleanValue);
                return converted.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            case Letter_Grading:
                switch (cleanValue){
                    case "A+": return new BigDecimal("1.0000");
                    case "A":  return new BigDecimal("0.9500");
                    case "A-": return new BigDecimal("0.9000");
                    case "B+": return new BigDecimal("0.8700");
                    case "B":  return new BigDecimal("0.8500");
                    case "B-": return new BigDecimal("0.8000");
                    case "C+": return new BigDecimal("0.7700");
                    case "C":  return new BigDecimal("0.7500");
                    case "C-": return new BigDecimal("0.7000");
                    case "D+": return new BigDecimal("0.6700");
                    case "D":  return new BigDecimal("0.6500");
                    case "D-": return new BigDecimal("0.6000");
                    case "F":  return new BigDecimal("0.0000");
                    default:
                        throw new IllegalArgumentException("Unknown letter grading: " + value);
                }
            case GPA_4_Point_Scale:
                converted = new BigDecimal(cleanValue);
                return converted.divide(new BigDecimal("4"), 4, RoundingMode.HALF_UP);
            case Numeric_Grading_1_to_5:
                converted = new BigDecimal(cleanValue);
                return converted.divide(new BigDecimal("5"), 4, RoundingMode.HALF_UP);
            case Numeric_Grading_1_to_12:
                converted = new BigDecimal(cleanValue);
                return converted.divide(new BigDecimal("12"), 4, RoundingMode.HALF_UP);
            case Numeric_Grading_1_to_10:
                converted = new BigDecimal(cleanValue);
                return converted.divide(new BigDecimal("10"), 4, RoundingMode.HALF_UP);
            case Numeric_Grading_1_to_20:
                converted = new BigDecimal(cleanValue);
                return converted.divide(new BigDecimal("20"), 4, RoundingMode.HALF_UP);
            case Numeric_Grading_5_to_1:
                converted = new BigDecimal(cleanValue);
                return new BigDecimal("5").subtract(converted)
                        .divide(new BigDecimal("4"), 4, RoundingMode.HALF_UP);
            case Numeric_Grading_6_to_1:
                converted = new BigDecimal(cleanValue);
                return new BigDecimal("6").subtract(converted)
                        .divide(new BigDecimal("5"), 4, RoundingMode.HALF_UP);
            case Pass_Fail:
                if("pass".equals(cleanValue)) return new BigDecimal("1.0000");
                if("fail".equals(cleanValue)) return new BigDecimal("0.0000");
                throw new IllegalArgumentException("Invalid value: " + value);
            default:
                throw new UnsupportedOperationException("Invalid grading system: " + system);
        }
    }

}
