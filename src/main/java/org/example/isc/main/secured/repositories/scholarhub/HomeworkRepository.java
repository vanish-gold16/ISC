package org.example.isc.main.secured.repositories.scholarhub;

import org.example.isc.main.secured.models.scholarship.DaySubject;
import org.example.isc.main.secured.models.scholarship.Homework;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    List<Homework> findAllByWeekStart(LocalDate weekStart);

    List<Homework> findAllByWeekStartAndDaySubject(LocalDate weekStart, DaySubject daySubject);

    List<Homework> findAllByWeekStartAndSubjectId(LocalDate weekStart, Long subjectId);

    Homework getById(Long id);
}
