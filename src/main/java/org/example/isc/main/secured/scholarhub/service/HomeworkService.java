package org.example.isc.main.secured.scholarhub.service;

import jakarta.transaction.Transactional;
import org.example.isc.main.dto.scholarship.HomeworkDTO;
import org.example.isc.main.secured.models.scholarship.DaySubject;
import org.example.isc.main.secured.models.scholarship.Homework;
import org.example.isc.main.secured.repositories.scholarhub.DaySubjectRepository;
import org.example.isc.main.secured.repositories.scholarhub.HomeworkRepository;
import org.springframework.stereotype.Service;

@Service
public class HomeworkService {

    private final DaySubjectRepository daySubjectRepository;
    private final HomeworkRepository homeworkRepository;

    public HomeworkService(DaySubjectRepository daySubjectRepository, HomeworkRepository homeworkRepository) {
        this.daySubjectRepository = daySubjectRepository;
        this.homeworkRepository = homeworkRepository;
    }

    @Transactional
    public void edit(
            Long id,
            HomeworkDTO homeworkDTO
    ){
        DaySubject lesson = daySubjectRepository.findById(homeworkDTO.getDueDaySubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + homeworkDTO.getDueDaySubjectId()));

        Homework homework = homeworkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Homework not found: " + id));

        homework.setTitle(homeworkDTO.getTitle());
        homework.setDetails(homeworkDTO.getDetails());
        homework.setPriority(homeworkDTO.getPriority());
        homework.setSubjectId(lesson.getSubject().getId());
        homework.setDueDaySubjectId(lesson.getId());
        homework.setStatus(homeworkDTO.getStatus());
        homework.setWeekStart(homeworkDTO.getWeekStart());

        homeworkRepository.save(homework);
    }

}
