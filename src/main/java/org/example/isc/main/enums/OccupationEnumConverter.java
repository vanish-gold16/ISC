package org.example.isc.main.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public class OccupationEnumConverter implements AttributeConverter<OccupationEnum, String> {
    @Override
    public String convertToDatabaseColumn(OccupationEnum attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public OccupationEnum convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();
        if ("0".equals(value)) {
            return OccupationEnum.IT;
        }
        if ("1".equals(value)) {
            return OccupationEnum.MEDICINE;
        }
        if ("2".equals(value)) {
            return OccupationEnum.ART;
        }
        if ("3".equals(value)) {
            return OccupationEnum.SPORTS;
        }
        if ("4".equals(value)) {
            return OccupationEnum.LAW;
        }

        return OccupationEnum.valueOf(value.toUpperCase());
    }
}
