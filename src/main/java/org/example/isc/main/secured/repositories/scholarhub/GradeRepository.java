package org.example.isc.main.secured.repositories.scholarhub;

import org.example.isc.main.secured.models.scholarship.Grade;
import org.example.isc.main.secured.models.scholarship.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findAllBySubjectIdAndSubjectUserUsername(Long subjectId, String username);

    List<Grade> findAllBySubjectUserUsername(String username);

    Optional<Grade> findByIdAndSubjectUserUsername(Long id, String username);

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query("delete from Grade g where g.assignedDaySubject.id in :daySubjectIds")
    void deleteAllByAssignedDaySubjectIdIn(Collection<Long> daySubjectIds);

    List<Grade> findAllBySubject(Subject subject);
}
