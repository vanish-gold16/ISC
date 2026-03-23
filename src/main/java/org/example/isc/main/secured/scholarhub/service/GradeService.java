package org.example.isc.main.secured.scholarhub.service;

import org.example.isc.main.dto.scholarship.GradeDTO;
import org.example.isc.main.secured.models.scholarship.ConvertGrade;
import org.example.isc.main.secured.models.scholarship.DaySubject;
import org.example.isc.main.secured.models.scholarship.Grade;
import org.example.isc.main.secured.models.scholarship.Subject;
import org.example.isc.main.secured.repositories.scholarhub.DaySubjectRepository;
import org.example.isc.main.secured.repositories.scholarhub.GradeRepository;
import org.example.isc.main.secured.repositories.scholarhub.SubjectsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GradeService {

    private final SubjectsRepository subjectsRepository;
    private final DaySubjectRepository daySubjectRepository;
    private final ConvertGrade convertGrade = new ConvertGrade();
    private final GradeRepository gradeRepository;

    public GradeService(
            SubjectsRepository subjectsRepository,
            DaySubjectRepository daySubjectRepository,
            GradeRepository gradeRepository
    ) {
        this.subjectsRepository = subjectsRepository;
        this.daySubjectRepository = daySubjectRepository;
        this.gradeRepository = gradeRepository;
    }

    public List<Grade> getAllForUser(String username) {
        return gradeRepository.findAllBySubjectUserUsername(username);
    }

    public Grade getForUser(Long id, String username) {
        return gradeRepository.findByIdAndSubjectUserUsername(id, username)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found: " + id));
    }

    public Grade create(GradeDTO gradeDTO, String username) {
        Subject subject = requireSubject(gradeDTO.getSubjectId(), username);
        DaySubject lesson = resolveLesson(gradeDTO.getAssignedDaySubjectId(), username, subject.getId());
        return saveGrade(new Grade(), gradeDTO, subject, lesson);
    }

    public Grade edit(Long id, GradeDTO gradeDTO, String username) {
        Grade grade = getForUser(id, username);
        Subject subject = requireSubject(gradeDTO.getSubjectId(), username);
        DaySubject lesson = resolveLesson(gradeDTO.getAssignedDaySubjectId(), username, subject.getId());
        return saveGrade(grade, gradeDTO, subject, lesson);
    }

    public void delete(Long id, String username) {
        Grade grade = getForUser(id, username);
        gradeRepository.delete(grade);
    }

    private Subject requireSubject(Long subjectId, String username) {
        return subjectsRepository.findById(subjectId)
                .filter(foundSubject -> username.equals(foundSubject.getUser().getUsername()))
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));
    }

    private Grade saveGrade(Grade grade, GradeDTO gradeDTO, Subject subject, DaySubject lesson) {
        String normalizedValue = convertGrade.normalizeValue(gradeDTO.getSystem(), gradeDTO.getValue());
        BigDecimal converted = convertGrade.toNormalizedScore(gradeDTO.getSystem(), normalizedValue);
        grade.setSubject(subject);
        grade.setAssignedDaySubject(lesson);
        grade.setGradingSystem(gradeDTO.getSystem());
        grade.setReason(gradeDTO.getReason());
        grade.setDescription(gradeDTO.getDescription());
        grade.setValue(normalizedValue);
        grade.setConverted(converted);
        return gradeRepository.save(grade);
    }

    private DaySubject resolveLesson(Long assignedDaySubjectId, String username, Long subjectId) {
        if (assignedDaySubjectId == null) {
            return null;
        }

        DaySubject lesson = daySubjectRepository.findByIdAndDayScheduleUserUsername(assignedDaySubjectId, username)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + assignedDaySubjectId));

        if (!subjectId.equals(lesson.getSubject().getId())) {
            throw new IllegalArgumentException("Lesson does not belong to subject: " + assignedDaySubjectId);
        }

        return lesson;
    }
}
