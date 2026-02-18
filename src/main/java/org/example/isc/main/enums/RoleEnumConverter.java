package org.example.isc.main.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoleEnumConverter implements AttributeConverter<RoleEnum, String> {

    @Override
    public String convertToDatabaseColumn(RoleEnum attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public RoleEnum convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();
        if ("0".equals(value)) {
            return RoleEnum.USER;
        }
        if ("1".equals(value)) {
            return RoleEnum.ADMIN;
        }

        return RoleEnum.valueOf(value.toUpperCase());
    }
}

