package org.example.isc.main.secured.repositories.scholarhub;

import org.example.isc.main.secured.models.scholarship.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    List<Homework> findAllByWeekStart(LocalDate weekStart);

    List<Homework> findAllByWeekStartAndSubjectId(LocalDate weekStart, Long subjectId);

    @Query("select h from Homework h where h.weekStart = :weekStart and h.dueDaySubject.id = :dueDaySubjectId")
    List<Homework> findAllByWeekStartAndDueDaySubjectId(LocalDate weekStart, Long dueDaySubjectId);

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query("delete from Homework h where h.dueDaySubject.id in :dueDaySubjectIds")
    void deleteAllByDueDaySubjectIdIn(Collection<Long> dueDaySubjectIds);

    Homework getById(Long id);
}
