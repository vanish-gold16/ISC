package org.example.isc.main.enums;

import java.util.Locale;

public enum OccupationEnum {
    IT,
    ENGINEERING,
    BUSINESS,
    FINANCE,
    LAW,
    MEDICINE,
    SCIENCE,
    EDUCATION,
    ART,
    SHOW_BUSINESS,
    DESIGN,
    MUSIC,
    MEDIA_AND_COMMUNICATION,
    HUMANITIES,
    SOCIAL_SCIENCES,
    SPORTS,
    TOURISM_AND_HOSPITALITY,
    AGRICULTURE_AND_ENVIRONMENT,
    TRANSPORT_AND_LOGISTICS,
    PUBLIC_SERVICE,
    MILITARY_AND_SECURITY,
    OTHER;

    public String getDisplayName() {
        String value = name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
