package org.example.isc.main.enums.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.isc.main.enums.NotificationEnum;
import org.example.isc.main.secured.models.Notification;

import java.lang.annotation.Annotation;

public class NotificationEnumConverter implements AttributeConverter<NotificationEnum, String> {
    @Override
    public String convertToDatabaseColumn(NotificationEnum attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public NotificationEnum convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();
        if ("0".equals(value)) {
            return NotificationEnum.LIKE;
        }
        if ("1".equals(value)) {
            return NotificationEnum.FOLLOW;
        }
        if("2".equals(value)) {
            return NotificationEnum.FRIEND_REQUEST;
        }
        if("3".equals(value)) {
            return NotificationEnum.COMMENT;
        }
        if("4".equals(value)) {
            return NotificationEnum.MESSAGE;
        }
        if("5".equals(value)) {
            return NotificationEnum.SYSTEM;
        }

        return NotificationEnum.valueOf(value.toUpperCase());
    }
}
