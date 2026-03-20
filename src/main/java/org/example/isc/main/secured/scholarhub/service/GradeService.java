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
        Subject subject = subjectsRepository.findById(gradeDTO.getSubjectId())
                .filter(foundSubject -> username.equals(foundSubject.getUser().getUsername()))
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + gradeDTO.getSubjectId()));

        DaySubject lesson = resolveLesson(gradeDTO.getAssignedDaySubjectId(), username, subject.getId());

        BigDecimal converted = convertGrade.toNormalizedScore(gradeDTO.getSystem(), gradeDTO.getValue());

        return gradeRepository.save(new Grade(
                subject,
                lesson,
                gradeDTO.getSystem(),
                gradeDTO.getReason(),
                gradeDTO.getValue(),
                converted
        ));
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
