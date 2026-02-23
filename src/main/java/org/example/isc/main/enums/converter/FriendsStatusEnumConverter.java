package org.example.isc.main.enums.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.isc.main.enums.FriendsStatusEnum;
import org.example.isc.main.enums.RoleEnum;

@Converter(autoApply = false)
public class FriendsStatusEnumConverter implements AttributeConverter<FriendsStatusEnum, String> {
    @Override
    public String convertToDatabaseColumn(FriendsStatusEnum attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public FriendsStatusEnum convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();
        if ("0".equals(value)) {
            return FriendsStatusEnum.PENDING;
        }
        if ("1".equals(value)) {
            return FriendsStatusEnum.ACCEPTED;
        }
        if("2".equals(value)) {
            return FriendsStatusEnum.DECLINED;
        }

        return FriendsStatusEnum.valueOf(value.toUpperCase());
    }
}
