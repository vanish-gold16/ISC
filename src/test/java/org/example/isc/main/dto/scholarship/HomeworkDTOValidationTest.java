package org.example.isc.main.dto.scholarship;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.example.isc.main.enums.scholarhub.HomeworkPriorityEnum;
import org.example.isc.main.enums.scholarhub.HomeworkStatusEnum;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HomeworkDTOValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsHomeworkTitleLongerThanThirtyCharacters() {
        HomeworkDTO homeworkDTO = validHomework();
        homeworkDTO.setTitle("realism and naturalism in literature");

        assertFalse(hasTitleViolation(homeworkDTO));
    }

    @Test
    void rejectsHomeworkTitleLongerThanConfiguredMaximum() {
        HomeworkDTO homeworkDTO = validHomework();
        homeworkDTO.setTitle("a".repeat(HomeworkDTO.TITLE_MAX_LENGTH + 1));

        assertTrue(hasTitleViolation(homeworkDTO));
    }

    private boolean hasTitleViolation(HomeworkDTO homeworkDTO) {
        return validator.validate(homeworkDTO).stream()
                .anyMatch(violation -> "title".equals(violation.getPropertyPath().toString()));
    }

    private HomeworkDTO validHomework() {
        HomeworkDTO homeworkDTO = new HomeworkDTO();
        homeworkDTO.setTitle("Essay");
        homeworkDTO.setPriority(HomeworkPriorityEnum._00FF00);
        homeworkDTO.setSubjectId(1L);
        homeworkDTO.setDueDaySubjectId(1L);
        homeworkDTO.setStatus(HomeworkStatusEnum.Pending);
        homeworkDTO.setWeekStart(LocalDate.of(2026, 3, 30));
        return homeworkDTO;
    }
}
