package org.example.isc.main.secured.scholarhub.service;

import org.example.isc.main.dto.scholarship.GradeDTO;
import org.example.isc.main.secured.models.scholarship.ConvertGrade;
import org.example.isc.main.secured.models.scholarship.DaySubject;
import org.example.isc.main.secured.models.scholarship.Grade;
import org.example.isc.main.secured.models.scholarship.Subject;
import org.example.isc.main.secured.repositories.scholarhub.DaySubjectRepository;
import org.example.isc.main.secured.repositories.scholarhub.SubjectsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class GradeService {

    private final SubjectsRepository subjectsRepository;
    private final DaySubjectRepository daySubjectRepository;
    private final ConvertGrade convertGrade = new ConvertGrade();

    public GradeService(SubjectsRepository subjectsRepository, DaySubjectRepository daySubjectRepository) {
        this.subjectsRepository = subjectsRepository;
        this.daySubjectRepository = daySubjectRepository;
    }

    public Grade create(GradeDTO gradeDTO){
        Subject subject = subjectsRepository.findById(gradeDTO.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + gradeDTO.getSubjectId()));
        DaySubject lesson = daySubjectRepository.findById(gradeDTO.getAssignedDaySubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + gradeDTO.getAssignedDaySubjectId()));

        BigDecimal converted = convertGrade.toNormalizedScore(gradeDTO.getSystem(), gradeDTO.getValue());

        if(converted == null) return null;

        return new Grade(
                subject,
                lesson,
                gradeDTO.getSystem(),
                gradeDTO.getValue(),
                converted
        );
    }

}
