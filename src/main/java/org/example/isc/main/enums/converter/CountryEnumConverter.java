package org.example.isc.main.enums.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.isc.main.enums.CountryEnum;

@Converter(autoApply = false)
public class CountryEnumConverter implements AttributeConverter<CountryEnum, String> {
    @Override
    public String convertToDatabaseColumn(CountryEnum attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public CountryEnum convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();
        if (value.chars().allMatch(Character::isDigit)) {
            int ordinal = Integer.parseInt(value);
            CountryEnum[] allCountries = CountryEnum.values();
            if (ordinal >= 0 && ordinal < allCountries.length) {
                return allCountries[ordinal];
            }
            throw new IllegalArgumentException("Unknown country ordinal: " + value);
        }

        return CountryEnum.valueOf(value.toUpperCase());
    }
}
