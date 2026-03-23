package org.example.isc.main.secured.scholarhub.service;

import jakarta.transaction.Transactional;
import org.example.isc.main.dto.scholarship.HomeworkDTO;
import org.example.isc.main.enums.scholarhub.GradeReasonEnum;
import org.example.isc.main.enums.scholarhub.GradingSystemEnum;
import org.example.isc.main.enums.scholarhub.HomeworkPriorityEnum;
import org.example.isc.main.enums.scholarhub.HomeworkStatusEnum;
import org.example.isc.main.secured.models.scholarship.ConvertGrade;
import org.example.isc.main.secured.models.scholarship.DaySubject;
import org.example.isc.main.secured.models.scholarship.Grade;
import org.example.isc.main.secured.models.scholarship.Homework;
import org.example.isc.main.secured.repositories.scholarhub.DaySubjectRepository;
import org.example.isc.main.secured.repositories.scholarhub.GradeRepository;
import org.example.isc.main.secured.repositories.scholarhub.HomeworkRepository;
import org.springframework.stereotype.Service;

@Service
public class HomeworkService {

    private final DaySubjectRepository daySubjectRepository;
    private final HomeworkRepository homeworkRepository;
    private final GradeRepository gradeRepository;
    private final ConvertGrade convertGrade = new ConvertGrade();

    public HomeworkService(
            DaySubjectRepository daySubjectRepository,
            HomeworkRepository homeworkRepository,
            GradeRepository gradeRepository
    ) {
        this.daySubjectRepository = daySubjectRepository;
        this.homeworkRepository = homeworkRepository;
        this.gradeRepository = gradeRepository;
    }

    @Transactional
    public Homework create(HomeworkDTO homeworkDTO) {
        DaySubject lesson = requireLesson(homeworkDTO.getDueDaySubjectId());

        Homework homework = new Homework();
        applyHomeworkFields(homework, homeworkDTO, lesson);
        homework = homeworkRepository.save(homework);

        upsertAttachedGrade(homework, homeworkDTO, lesson);
        return homeworkRepository.save(homework);
    }

    @Transactional
    public Homework edit(Long id, HomeworkDTO homeworkDTO) {
        DaySubject lesson = requireLesson(homeworkDTO.getDueDaySubjectId());
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Homework not found: " + id));

        applyHomeworkFields(homework, homeworkDTO, lesson);
        upsertAttachedGrade(homework, homeworkDTO, lesson);
        return homeworkRepository.save(homework);
    }

    @Transactional
    public void delete(Long id) {
        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Homework not found: " + id));

        Long gradeId = homework.getGradeId();
        homeworkRepository.delete(homework);
        if (gradeId != null) {
            gradeRepository.deleteById(gradeId);
        }
    }

    private DaySubject requireLesson(Long dueDaySubjectId) {
        return daySubjectRepository.findById(dueDaySubjectId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + dueDaySubjectId));
    }

    private void applyHomeworkFields(Homework homework, HomeworkDTO homeworkDTO, DaySubject lesson) {
        homework.setTitle(homeworkDTO.getTitle());
        homework.setDetails(homeworkDTO.getDetails());
        homework.setPriority(homeworkDTO.getPriority());
        homework.setSubjectId(lesson.getSubject().getId());
        homework.setDueDaySubject(lesson);
        homework.setStatus(homeworkDTO.getStatus());
        homework.setWeekStart(homeworkDTO.getWeekStart());
    }

    private void upsertAttachedGrade(Homework homework, HomeworkDTO homeworkDTO, DaySubject lesson) {
        String gradeValue = homeworkDTO.getGradeValue() != null ? homeworkDTO.getGradeValue().trim() : "";
        if (gradeValue.isBlank()) {
            return;
        }

        GradingSystemEnum system = homeworkDTO.getGradeSystem() != null
                ? homeworkDTO.getGradeSystem()
                : GradingSystemEnum.Numeric_Grading_1_to_5;

        Grade grade = homework.getGradeId() != null
                ? gradeRepository.findById(homework.getGradeId())
                    .orElseGet(Grade::new)
                : new Grade();

        grade.setSubject(lesson.getSubject());
        grade.setAssignedDaySubject(lesson);
        grade.setGradingSystem(system);
        grade.setReason(resolveGradeReason(homeworkDTO.getPriority()));
        grade.setDescription(buildGradeDescription(homework.getTitle(), homework.getDetails()));
        String normalizedGradeValue = convertGrade.normalizeValue(system, gradeValue);
        grade.setValue(normalizedGradeValue);
        grade.setConverted(convertGrade.toNormalizedScore(system, normalizedGradeValue));

        Grade savedGrade = gradeRepository.save(grade);
        homework.setGradeId(savedGrade.getId());
        homework.setStatus(HomeworkStatusEnum.Graded);
    }

    private GradeReasonEnum resolveGradeReason(HomeworkPriorityEnum priority) {
        return priority == HomeworkPriorityEnum._6B21A8
                ? GradeReasonEnum.Exam
                : GradeReasonEnum.Activity;
    }

    private String buildGradeDescription(String title, String details) {
        String normalizedTitle = title != null ? title.trim() : "";
        String normalizedDetails = details != null ? details.trim() : "";

        if (!normalizedTitle.isBlank() && !normalizedDetails.isBlank()) {
            return "Attached to task: " + normalizedTitle + ". " + normalizedDetails;
        }
        if (!normalizedTitle.isBlank()) {
            return "Attached to task: " + normalizedTitle;
        }
        return normalizedDetails.isBlank() ? null : normalizedDetails;
    }
}
